package method;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

/**
 * @author Derrick Lockwood
 * @created 6/26/18.
 */
public class AttributeClass {

    public static final String GENERICS = "generics";
    public static final String IS_ARRAY = "is_array";
    public static final String IS_LIST = "is_list";
    public static final String DIMENSIONS = "dimensions";
    public static final String TYPE = "type";
    public static final String IS_PRIMITIVE = "is_primitive";

    private final HashMap<String, Object> attributes;
    private final Class<?> mockClass;

    public AttributeClass(Class<?> mockClass) {
        this.attributes = new HashMap<>();
        this.mockClass = mockClass;
    }

    public static AttributeClass createAttributeClass(String type) throws ClassNotFoundException {
        return createAttributeClass(type, ClassLoader.getSystemClassLoader());
    }

    public static AttributeClass createAttributeClass(JSONObject jsonObject) throws ClassNotFoundException {
        return createAttributeClass(jsonObject, ClassLoader.getSystemClassLoader());
    }

    public static AttributeClass createAttributeClass(String type, ClassLoader classLoader) throws ClassNotFoundException {
        if (type == null) {
            return null;
        }
        AttributeClass attributeClass;
        if (type.contains("<")) {
            String[] splits = type.split("<");
            String mockType = splits[0];
            String after = splits[1];
            after = after.replace(">", "");
            String[] generics = after.split(",");
            attributeClass = new AttributeClass(Class.forName(mockType, true, classLoader));
            AttributeClass[] genericClasses = new AttributeClass[generics.length];
            for (int i = 0; i < generics.length; i++) {
                genericClasses[i] = createAttributeClass(generics[i], classLoader);
            }
            attributeClass.setAttribute(GENERICS, genericClasses);
            attributeClass.setAttribute(TYPE, attributeClass.getMockClass());
        } else if (type.contains("[")) {
            String mockType = type.substring(type.lastIndexOf('[') + 1);
            int[] dimensions = new int[(int) type.chars().filter(ch -> ch == '[').count()];
            attributeClass = new AttributeClass(getClassForString(fromArrayName(mockType), classLoader));
            attributeClass.setAttribute(IS_ARRAY, true);
            attributeClass.setAttribute(DIMENSIONS, dimensions);
            attributeClass.setAttribute(TYPE, Class.forName(type.replace(mockType, toArrayName(mockType)), true, classLoader));
        } else {
            attributeClass = new AttributeClass(getClassForString(type, classLoader));
            attributeClass.setAttribute(TYPE, attributeClass.getMockClass());
        }
        setIsPrimitive(attributeClass);
        return attributeClass;
    }

    public static AttributeClass createAttributeClass(JSONObject jsonObject, ClassLoader classLoader) throws ClassNotFoundException {
        AttributeClass attributeClass;
        String type = (String) jsonObject.get("class");
        if (type.contains("List") && jsonObject.containsKey("shape")) {
            attributeClass = parseList(jsonObject, classLoader, type);
        } else if (type.contains("<")) {
            attributeClass = parseGenerics(jsonObject, classLoader, type);
        } else if (jsonObject.containsKey("shape")) {
            attributeClass = parseArray(jsonObject, classLoader, type);
        } else {
            attributeClass = new AttributeClass(getClassForString(type, classLoader));
            attributeClass.setAttribute(TYPE, attributeClass.getMockClass());
        }
        setIsPrimitive(attributeClass);
        return attributeClass;
    }

    private static AttributeClass parseList(JSONObject jsonObject, ClassLoader classLoader, String type) throws ClassNotFoundException {
        String[] splits = type.split("<");
        String realType = splits[0];
        String mockType = splits[1];
        mockType = mockType.substring(0, mockType.length() - 1);
        AttributeClass attributeClass = new AttributeClass(getClassForString(mockType, classLoader));
        JSONArray shape = (JSONArray) jsonObject.get("shape");
        int[] dimensions = new int[shape.size()];
        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = (int) ((long) shape.get(i));
        }
        attributeClass.setAttribute(DIMENSIONS, dimensions);
        attributeClass.setAttribute(IS_LIST, true);
        attributeClass.setAttribute(TYPE, getClassForString(realType, classLoader));
        return attributeClass;
    }

    private static AttributeClass parseArray(JSONObject jsonObject, ClassLoader classLoader, String type) throws ClassNotFoundException {
        JSONArray shape = (JSONArray) jsonObject.get("shape");
        AttributeClass attributeClass = new AttributeClass(getClassForString(type, classLoader));
        int[] dimensions = new int[shape.size()];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = (int) ((long) shape.get(i));
            stringBuilder.append('[');
        }
        attributeClass.setAttribute(IS_ARRAY, true);
        attributeClass.setAttribute(DIMENSIONS, dimensions);
        stringBuilder.append(toArrayName(type));
        attributeClass.setAttribute(TYPE, Class.forName(stringBuilder.toString(), true, classLoader));
        return attributeClass;
    }

    private static AttributeClass parseGenerics(JSONObject jsonObject, ClassLoader classLoader, String type) throws ClassNotFoundException {
        //TODO: parse various generic thoughts: Foo<Integer> mock Foo Foo<Foo<Integer>> Mock Foo then Foo?
        String[] splits = type.split("<");
        String mockType = splits[0];
        String after = splits[1];
        after = after.substring(0, after.length() - 1);
        String[] generics = after.split(",");
        AttributeClass attributeClass = new AttributeClass(Class.forName(mockType, true, classLoader));
        AttributeClass[] genericClasses = new AttributeClass[generics.length];
        for (int i = 0; i < generics.length; i++) {
            genericClasses[i] = createAttributeClass(generics[i]);
        }
        attributeClass.setAttribute(GENERICS, genericClasses);
        attributeClass.setAttribute(TYPE, attributeClass.getMockClass());
        return attributeClass;
    }

    private static void setIsPrimitive(AttributeClass attributeClass) {
        if (attributeClass.getMockClass().isPrimitive() ||
                attributeClass.getMockClass().isAssignableFrom(Byte.class) ||
                attributeClass.getMockClass().isAssignableFrom(Character.class) ||
                attributeClass.getMockClass().isAssignableFrom(Boolean.class) ||
                attributeClass.getMockClass().isAssignableFrom(Short.class) ||
                attributeClass.getMockClass().isAssignableFrom(Float.class) ||
                attributeClass.getMockClass().isAssignableFrom(Integer.class) ||
                attributeClass.getMockClass().isAssignableFrom(Long.class) ||
                attributeClass.getMockClass().isAssignableFrom(Double.class) ||
                attributeClass.getMockClass().isAssignableFrom(String.class)) {
            attributeClass.setAttribute(IS_PRIMITIVE, true);
        } else {
            attributeClass.setAttribute(IS_PRIMITIVE, false);
        }
    }

    private static String fromArrayName(String name) {
        switch (name) {
            case "Z":
                return "boolean";
            case "B":
                return "byte";
            case "C":
                return "char";
            case "S":
                return "short";
            case "I":
                return "int";
            case "J":
                return "long";
            case "F":
                return "float";
            case "D":
                return "double";
            default:
                if (name.startsWith("[")) {
                    return name.substring(1, name.length());
                }
                return name;
        }
    }

    private static String toArrayName(String name) {
        switch (name) {
            case "boolean":
                return "Z";
            case "byte":
                return "B";
            case "char":
                return "C";
            case "short":
                return "S";
            case "int":
                return "I";
            case "long":
                return "J";
            case "float":
                return "F";
            case "double":
                return "D";
            default:
                return "L" + name + ";";
        }
    }

    private static Class<?> getClassForString(String name, ClassLoader classLoader) throws ClassNotFoundException {
        switch (name) {
            case "Boolean":
            case "boolean":
                return boolean.class;
            case "Byte":
            case "byte":
                return byte.class;
            case "Character":
            case "char":
                return char.class;
            case "Short":
            case "short":
                return short.class;
            case "Integer":
            case "int":
                return int.class;
            case "Long":
            case "long":
                return long.class;
            case "Float":
            case "float":
                return float.class;
            case "Double":
            case "double":
                return double.class;
            default:
                return Class.forName(name, true, classLoader);
        }
    }

    public void setAttribute(String name, Object attribute) {
        attributes.put(name, attribute);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.getOrDefault(name, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, T defaultAttribute) {
        return (T) attributes.getOrDefault(name, defaultAttribute);
    }

    public Class<?> getRealClass() {
        return getAttribute(TYPE);
    }

    public Class<?> getMockClass() {
        return mockClass;
    }

    @Override
    public String toString() {
        Class<?> type = getRealClass();
        return (type == null ? "" : type.toString()) + mockClass;
    }
}
