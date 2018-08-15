package method;

import method.callbacks.EmptyMethodCallback;
import method.callbacks.MethodCallback;

/**
 * @author Derrick Lockwood
 * @created 8/7/18.
 */
public class MethodCallFactory {
    /**
     * Creates a mocked method call that is determined by {@code methodName} and {@code parameterTypes} which is then used to
     * supply functionality to the mocked method. The method callback is used to control the flow of the method to be run.
     * <p>
     * Note: Everything is createObject the form of a callback and thus should only have to create objects once if not a primitive unless
     * otherwise specified
     *
     * @param methodCallback callback to send back the method data and control how many times the method is run
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found createObject {@code type}
     */
    public static MethodCall createMethodCall(
            MethodCallback methodCallback,
            Class<?> type,
            String methodName,
            AttributeClass... parameterTypes) throws NoSuchMethodException {
        Class<?>[] classes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            classes[i] = parameterTypes[i].getRealClass();
        }
        return new MethodCall(parameterTypes, type.getDeclaredMethod(methodName, classes), methodCallback);
    }

    public static MethodCall createMethodCall(Class<?> type,
            String methodName,
            AttributeClass... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(EmptyMethodCallback.create(), type, methodName, parameterTypes);
    }

    /**
     * Creates a mocked method call that is determined by {@code methodName} and {@code parameterTypes} which is then used to
     * supply functionality to the mocked method. The method callback is used to control the flow of the method to be run.
     * <p>
     * Note: Everything is createObject the form of a callback and thus should only have to create objects once if not a primitive unless
     * otherwise specified
     *
     * @param methodCallback callback to send back the method data and control how many times the method is run
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found createObject {@code type}
     */
    public static MethodCall createMethodCall(
            MethodCallback methodCallback,
            Class<?> type,
            String methodName,
            Class<?>... parameterTypes) throws NoSuchMethodException {
        AttributeClass[] attributeClasses = new AttributeClass[parameterTypes.length];
        for (int i = 0; i < attributeClasses.length; i++) {
            attributeClasses[i] = new AttributeClass(parameterTypes[i]);
        }
        return new MethodCall(attributeClasses, type.getDeclaredMethod(methodName, parameterTypes), methodCallback);
    }


    public static MethodCall createMethodCall(
            MethodCallback methodCallback,
            Class<?> type,
            String methodName,
            String... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        AttributeClass[] attributeClasses = parseParameterTypes(parameterTypes);
        Class<?>[] classes = new Class[attributeClasses.length];
        for (int i = 0; i < attributeClasses.length; i++) {
            classes[i] = attributeClasses[i].getRealClass();
        }
        return new MethodCall(attributeClasses, type.getDeclaredMethod(methodName, classes), methodCallback);
    }

    public static MethodCall createMethodCall(Class<?> type,
            String methodName,
            String... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return createMethodCall(EmptyMethodCallback.create(), type, methodName, parameterTypes);
    }

    private static AttributeClass[] parseParameterTypes(String... parameterTypes) throws ClassNotFoundException {
        AttributeClass[] arr = new AttributeClass[parameterTypes == null ? 0 : parameterTypes.length];
        for (int i = 0; i < (parameterTypes != null ? parameterTypes.length : 0); i++) {
            arr[i] = AttributeClass.createAttributeClass(parameterTypes[i]);
        }
        return arr;
    }
}
