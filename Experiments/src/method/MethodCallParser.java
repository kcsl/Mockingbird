package method;

import mock.MockClass;
import mock.MockCreator;
import mock.answers.*;
import mock.answers.readers.inputstream.ByteReaderInputStream;
import mock.answers.readers.inputstream.ByteReaderInputStreamList;
import mock.answers.readers.inputstream.DefaultByteReaderInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class MethodCallParser {
    public static Level VERBOSITY = Level.CONFIG;
    private static Logger LOGGER = Logger.getLogger(MethodCallParser.class.getName());

    private MethodCallParser() {
    }

    private static void checkKeys(JSONObject object, String... keys) {
        for (String key : keys) {
            if (!object.containsKey(key)) {
                throw new MethodCallConfigKeyException(key, object);
            }
        }
    }

    private static Class<?> getClassForString(String name) throws ClassNotFoundException {
        switch (name) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                return Class.forName(name);
        }
    }

    private static String arrayName(String name) {
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
                return "L";
            case "float":
                return "F";
            case "double":
                return "D";
            default:
                return name;
        }
    }

    private static Class<?> getClassForString(String name, String multiName) throws ClassNotFoundException {
        if (multiName != null) {
            switch (multiName) {
                case "array":
                    return Class.forName("[" + arrayName(name));
            }
        }
        return getClassForString(name);
    }

    private static ConstructParamAnswer parseConstruct(String readerName, MethodCall methodCall, JSONObject construct,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            NoSuchFieldException,
            ClassNotFoundException {
        LOGGER.log(Level.INFO, "Parsing Construct");
        ConstructParamAnswer answer;
        if (!construct.containsKey("params")) {
            answer = new ConstructParamAnswer(null, null);
        } else {
            JSONObject jsonObject = (JSONObject) construct.get("params");
            Set<String> keys = jsonObject.keySet();
            Class<?>[] classes = keys.stream().<Class<?>>map((obj) -> {
                try {
                    return getClassForString(obj);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).toArray((IntFunction<Class<?>[]>) Class[]::new);
            Iterator iterator = keys.iterator();
            Answer[] answers = new Answer[classes.length];
            for (int i = 0; i < answers.length; i++) {
                answers[i] = parseConstraint(readerName + ":" + i, methodCall,
                        (JSONObject) jsonObject.get(iterator.next()), byteReaders);
            }
            answer = new ConstructParamAnswer(classes, answers);
        }
        LOGGER.log(Level.INFO, "End Parsing Construct");
        return answer;
    }

    private static Answer parseConstraint(String readerName, MethodCall methodCall, JSONObject constraint,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        if (constraint == null) {
            ByteReaderInputStream byteReader = new DefaultByteReaderInputStream(readerName);
            byteReaders.add(byteReader);
            return byteReader;
        }
        checkKeys(constraint, "type");
        String type = (String) constraint.get("type");
        LOGGER.log(VERBOSITY, "Started Parsing Constraint type " + type);
        Answer answer = null;
        if (methodCall != null) {
            switch (type) {
                case "class":
                    if (constraint.containsKey("name")) {
                        answer = new ObjectInstantiatorAnswer(methodCall.getObjectInstantiatorByName(
                                (String) constraint.get("name")));
                    } else {
                        answer = new ObjectInstantiatorAnswer(parseMockClass(methodCall, constraint, byteReaders));
                    }
                    break;
                case "method":
                    String name = (String) constraint.get("name");
                    String[] splits = name.split(".");
                    ObjectInstantiator<?> objectInstantiator = methodCall.getObjectInstantiatorByName(splits[0]);
                    MockClass mockClass = objectInstantiator instanceof MockClass ? (MockClass) objectInstantiator : null;
                    if (mockClass != null) {
                        String[] methodFeatures = splits[0].split("[(]");
                        String methodName = methodFeatures[0];
                        methodFeatures[1] = methodFeatures[1].replace(")", "");
                        String[] parameters = methodFeatures[1].split(",");
                        Class<?>[] params = new Class[parameters.length];
                        for (int i = 0; i < parameters.length; i++) {
                            params[i] = getClassForString(parameters[i]);
                        }
                        answer = mockClass.getAnswer(methodName, params);
                    } else {
                        LOGGER.log(Level.WARNING,
                                "Parsing of constraint object instantiator needs to be a MockClass to get reference methods");
                        answer = null;
                    }
                    break;
            }
        }
        if (answer == null) {
            switch (type) {
                case "construct":
                    answer = parseConstruct(readerName, methodCall, constraint, byteReaders);
                    break;
                case "param":
                    int index = (int) constraint.get("index");
                    answer = new ParameterIndexAnswer(index);
                    break;
                case "void":
                    answer = new EmptyAnswer();
                    break;
                case "fixed":
                    answer = new FixedAnswer(constraint.get("value"));
                    break;
                case "original":
                    answer = new OriginalMethodAnswer();
                    break;
                default:
                    BasicAnswer[] staticObjects = null;
                    if (constraint.containsKey("static")) {
                        JSONArray array = (JSONArray) constraint.get("static");
                        staticObjects = new BasicAnswer[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            Object object = array.get(i);
                            if (object instanceof JSONObject) {
                                staticObjects[i] = BasicAnswer.transform(parseConstraint(readerName + ":staticObj:" + i, methodCall,
                                        (JSONObject) object, byteReaders));
                            } else {
                                if (object instanceof Long) {
                                    object = Math.toIntExact((Long) object);
                                }
                                staticObjects[i] = new FixedAnswer(object);
                            }

                        }
                    }
                    ByteReaderInputStream byteReader = ByteReaderInputStream.createByType(readerName, type, constraint,
                            staticObjects);
                    byteReaders.add(byteReader);
                    answer = byteReader;
                    break;
            }
        }

        if (constraint.containsKey("constraint") && answer != null) {
            answer = answer.link(
                    parseConstraint(readerName, methodCall, (JSONObject) constraint.get("constraint"), byteReaders));
        }
        LOGGER.log(VERBOSITY, "Completed Parsing Constraint type " + type);
        return answer;
    }

    private static void parseMockMethod(MockClass mockClass, MethodCall methodCall, JSONObject method,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        String name = (String) method.get("name");
        LOGGER.log(VERBOSITY, "Started Parsing Method " + name);
        Class<?>[] parameterClassesArray;
        if (method.containsKey("parameters")) {
            JSONArray parameterArray = (JSONArray) method.get("parameters");
            parameterClassesArray = new Class[parameterArray.size()];
            for (int k = 0; k < parameterArray.size(); k++) {
                parameterClassesArray[k] = getClassForString((String) parameterArray.get(k));
            }
        } else {
            parameterClassesArray = new Class[0];
        }
        mockClass.applyMethod(parseConstraint(name, methodCall, (JSONObject) method.get("constraint"), byteReaders),
                name,
                parameterClassesArray);
        LOGGER.log(VERBOSITY, "Completed Parsing Method " + name + " | " + Arrays.toString(parameterClassesArray));
    }

    private static void parseMockMethods(MockClass mockClass, MethodCall methodCall, JSONArray methods,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            NoSuchFieldException,
            ClassNotFoundException {
        LOGGER.log(VERBOSITY, "Started Parsing Mock Methods");
        if (methods != null) {
            for (Object methodObj : methods) {
                JSONObject method = (JSONObject) methodObj;
                parseMockMethod(mockClass, methodCall, method, byteReaders);
            }
        } else {
            LOGGER.log(Level.CONFIG,
                    "No definition of methods supplied to class " + mockClass.getOldType().getName() + " Skipping...");
        }
        LOGGER.log(VERBOSITY, "Completed Parsing Mock Methods");
    }

    private static void parseInstanceVariable(MockClass fieldClass, MethodCall methodCall, String name,
            JSONObject instanceVariable, ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Parsing of Instance Variable " + name);
        if (instanceVariable.containsKey("constraint")) { //i.e. primitive
            LOGGER.log(VERBOSITY, "Instance variable primitive");
            JSONObject constraint = (JSONObject) instanceVariable.get("constraint");
            Field field = fieldClass.getOldType().getDeclaredField(name);
            fieldClass.applyField(field,
                    new AnswerInstantiator(parseConstraint(name, methodCall, constraint, byteReaders),
                            field.getType()));
        } else if (instanceVariable.containsKey("methods")) { //i.e. object
            LOGGER.log(VERBOSITY, "Instance variable object");
            fieldClass.applyField(name, parseMockClass(methodCall, instanceVariable, byteReaders));
        } else { //i.e. primitive no constraint
            LOGGER.log(VERBOSITY, "Instance variable primitive no constraint");
            ByteReaderInputStream byteReader = new DefaultByteReaderInputStream(name);
            byteReaders.add(byteReader);
            Field field = fieldClass.getOldType().getDeclaredField(name);
            fieldClass.applyField(field, new AnswerInstantiator(byteReader, field.getType()));
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Instance Variable " + name);
    }

    private static void parseInstanceVariables(MockClass mockClass, MethodCall methodCall, JSONArray instanceVariables,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            NoSuchFieldException,
            ClassNotFoundException {
        LOGGER.log(VERBOSITY, "Started Parsing of Instance Variables");
        if (instanceVariables != null) {
            for (Object object : instanceVariables) {
                JSONObject instanceVariable = (JSONObject) object;
                String name = (String) instanceVariable.get("name");
                if (name == null) {
                    continue;
                }
                parseInstanceVariable(mockClass, methodCall, name, instanceVariable, byteReaders);
            }
        } else {
            LOGGER.log(Level.CONFIG,
                    "No definition of Instance Variables supplied to class " + mockClass.getOldType().getName() + " Skipping...");
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Instance Variables");
    }

    private static void parseMockClass(MockClass mockClass, MethodCall methodCall, JSONObject mockClassObject,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Parsing of Mock Class " + mockClass.getOldType().getName());
        JSONArray jsonArray = (JSONArray) mockClassObject.get("methods");
        String name = (String) mockClassObject.get("name");
        if (name != null) {
            mockClass.setName(name);
        }
        parseMockMethods(mockClass, methodCall, jsonArray, byteReaders);

        jsonArray = (JSONArray) mockClassObject.get("instance_variables");
        parseInstanceVariables(mockClass, methodCall, jsonArray, byteReaders);
        LOGGER.log(VERBOSITY, "Completed Parsing of Mock Class " + mockClass.getOldType().getName());
    }

    private static MockCreator parseMockClass(AttributeClass attributeClass, MethodCall methodCall,
            JSONObject mockClass,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        MockCreator subMockClass = methodCall.createStoredMock(attributeClass);
        if (subMockClass instanceof MockClass) {
            parseMockClass((MockClass) subMockClass, methodCall, mockClass, byteReaders);
        }
        return subMockClass;
    }

    private static MockCreator parseMockClass(MethodCall methodCall, JSONObject mockClass,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        return parseMockClass(AttributeClass.createAttributeClass(mockClass), methodCall,
                mockClass, byteReaders);
    }

    private static void parseParameter(MethodCall methodCall, int index, JSONObject parameter,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        AttributeClass attributeClass = methodCall.getParameterAttributeClass(index);
        LOGGER.log(VERBOSITY, "Started Parsing of Parameter " + attributeClass.getAttribute(AttributeClass.TYPE));
        if (attributeClass.getAttribute(AttributeClass.IS_PRIMITIVE)) {
            JSONObject constraint = (JSONObject) parameter.get("constraint");
            methodCall.createParameterMock(index,
                    parseConstraint("parameter-" + index, methodCall, constraint, byteReaders));
            LOGGER.log(VERBOSITY, "Parsed Primitive Parameter");
        } else {
            parseMockClass(methodCall.createParameterMock(index), methodCall, parameter, byteReaders);
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Parameter " + attributeClass.getAttribute(AttributeClass.TYPE));
    }

    private static void parseParameters(MethodCall methodCall, JSONArray parameters,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        if (parameters == null) {
            return;
        }
        LOGGER.log(VERBOSITY, "Started Parsing of Parameters");
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject object = (JSONObject) parameters.get(i);
            parseParameter(methodCall, i, object, byteReaders);
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Parameters");
    }

    private static MethodCall createMethodCall(JSONObject methodCallObject,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException, NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Creating Method Call");
        Class<?> mockClass = getClassForString((String) methodCallObject.get("class"));
        String methodName = (String) methodCallObject.get("method");
        ConstructParamAnswer constructParamAnswer = null;
        if (methodCallObject.containsKey("construct")) {
            constructParamAnswer = parseConstruct(mockClass.getName(), null,
                    (JSONObject) methodCallObject.get("construct"), byteReaders);
        }
        JSONArray parameters = (JSONArray) methodCallObject.get("parameters");
        AttributeClass[] parameterTypes;
        if (parameters != null) {
            parameterTypes = new AttributeClass[parameters.size()];
            for (int i = 0; i < parameterTypes.length; i++) {
                JSONObject object = (JSONObject) parameters.get(i);
                parameterTypes[i] = AttributeClass.createAttributeClass(object);
            }
        } else {
            parameterTypes = new AttributeClass[0];
        }

        LOGGER.log(VERBOSITY,
                "Instrumented Class Path: " + mockClass.getProtectionDomain().getCodeSource().getLocation().getPath());
        MethodCall methodCall = MethodCallFactory.createMethodCall(mockClass, methodName, constructParamAnswer,
                parameterTypes);
        LOGGER.log(VERBOSITY, "Finished Creating Method Call");
        return methodCall;
    }

    public static MethodCall setupMethodCall(Logger logger, JSONObject definition,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        LOGGER.setParent(logger);
        LOGGER.log(VERBOSITY, "Started Parsing of Method Call");
        MethodCall methodCall = createMethodCall(definition, byteReaders);
        parseParameters(methodCall, (JSONArray) definition.get("parameters"), byteReaders);
        parseInstanceVariables(methodCall.getMethodMockClass(), methodCall,
                (JSONArray) definition.get("instance_variables"), byteReaders);
        parseMockMethods(methodCall.getMethodMockClass(), methodCall, (JSONArray) definition.get("methods"),
                byteReaders);
        LOGGER.log(VERBOSITY, "Created Method Call " + methodCall);
        return methodCall;
    }

    public static class MethodCallConfigKeyException extends RuntimeException {
        public MethodCallConfigKeyException(String key, JSONObject object) {
            super("Key " + key + " not found in " + object.toJSONString());
        }
    }


}
