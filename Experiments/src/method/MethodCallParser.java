package method;

import mock.MockClass;
import mock.SubMockClass;
import mock.answers.*;
import mock.answers.readers.ByteReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
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

    private static Answer parseConstraint(MethodCall methodCall, JSONObject constraint,
            List<ByteReader> byteReaders) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        if (constraint == null) {
            ByteReader byteReader = ByteReader.createDefault();
            byteReaders.add(byteReader);
            return byteReader;
        }
        checkKeys(constraint, "type");
        String type = (String) constraint.get("type");
        LOGGER.log(VERBOSITY, "Started Parsing Constraint type " + type);
        Answer answer;
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
            case "param":
                int index = (int) constraint.get("index");
                answer = (ParameterAnswer) parameters -> parameters[index];
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
                ByteReader byteReader = ByteReader.getByType(type, constraint);
                byteReaders.add(byteReader);
                answer = byteReader;
                break;
        }
        if (constraint.containsKey("constraint") && answer != null) {
            answer = answer.link(parseConstraint(methodCall, (JSONObject) constraint.get("constraint"), byteReaders));
        }
        LOGGER.log(VERBOSITY, "Completed Parsing Constraint type " + type);
        return answer;
    }

    private static void parseMockMethod(SubMockClass subMockClass, MethodCall methodCall, JSONObject method,
            List<ByteReader> byteReaders) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
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
        subMockClass.applyMethod(parseConstraint(methodCall, (JSONObject) method.get("constraint"), byteReaders), name,
                parameterClassesArray);
        LOGGER.log(VERBOSITY, "Completed Parsing Method " + name + " | " + Arrays.toString(parameterClassesArray));
    }

    private static void parseMockClass(SubMockClass subMockClass, MethodCall methodCall, JSONObject mockClass,
            List<ByteReader> byteReaders) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Parsing of Mock Class " + subMockClass.getOldType().getName());
        JSONArray jsonArray = (JSONArray) mockClass.get("methods");
        String name = (String) mockClass.get("name");
        if (name != null) {
            subMockClass.setName(name);
        }
        if (jsonArray != null) {
            for (Object aJsonArray : jsonArray) {
                JSONObject method = (JSONObject) aJsonArray;
                parseMockMethod(subMockClass, methodCall, method, byteReaders);
            }
        } else {
            LOGGER.log(Level.CONFIG,
                    "No definition of methods supplied to class " + subMockClass.getOldType().getName());
        }

        jsonArray = (JSONArray) mockClass.get("instance_variables");
        if (jsonArray != null) {
            parseInstanceVariables(subMockClass, methodCall, jsonArray, byteReaders);
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Mock Class " + subMockClass.getOldType().getName());
    }

    private static SubMockClass parseMockClass(Class<?> type, MethodCall methodCall, JSONObject mockClass,
            List<ByteReader> byteReaders) throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        SubMockClass subMockClass = methodCall.createStoredMock(type);
        parseMockClass(subMockClass, methodCall, mockClass, byteReaders);
        return subMockClass;
    }

    private static SubMockClass parseMockClass(MethodCall methodCall, JSONObject mockClass,
            List<ByteReader> byteReaders) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        return parseMockClass(Class.forName((String) mockClass.get("class")), methodCall, mockClass, byteReaders);
    }

    private static void parseInstanceVariable(MockClass fieldClass, MethodCall methodCall, String name,
            JSONObject instanceVariable, List<ByteReader> byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Parsing of Instance Variable " + name);
        if (instanceVariable.containsKey("constraint")) { //i.e. primitive
            LOGGER.log(VERBOSITY, "Instance variable primitive");
            JSONObject constraint = (JSONObject) instanceVariable.get("constraint");
            Field field = fieldClass.getOldType().getDeclaredField(name);
            fieldClass.applyField(field,
                    new AnswerInstantiator(parseConstraint(methodCall, constraint, byteReaders), field.getType()));
        } else if (instanceVariable.containsKey("methods")) { //i.e. object
            LOGGER.log(VERBOSITY, "Instance variable object");
            fieldClass.applyField(name, parseMockClass(methodCall, instanceVariable, byteReaders));
        } else { //i.e. primitive no constraint
            LOGGER.log(VERBOSITY, "Instance variable primitive no constraint");
            ByteReader byteReader = ByteReader.createDefault();
            byteReaders.add(byteReader);
            Field field = fieldClass.getOldType().getDeclaredField(name);
            fieldClass.applyField(field, new AnswerInstantiator(byteReader, field.getType()));
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Instance Variable " + name);
    }

    private static void parseInstanceVariables(MockClass fieldClass, MethodCall methodCall, JSONArray instanceVariables,
            List<ByteReader> byteReaders) throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
        if (instanceVariables == null) {
            return;
        }
        LOGGER.log(VERBOSITY, "Started Parsing of Instance Variables");
        for (Object object : instanceVariables) {
            JSONObject instanceVariable = (JSONObject) object;
            String name = (String) instanceVariable.get("name");
            if (name == null) {
                continue;
            }
            parseInstanceVariable(fieldClass, methodCall, name, instanceVariable, byteReaders);
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Instance Variables");
    }

    private static void parseParameter(MethodCall methodCall, int index, JSONObject parameter,
            List<ByteReader> byteReaders) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        if (parameter.containsKey("constraint")) { //i.e. primitive
            JSONObject constraint = (JSONObject) parameter.get("constraint");
            methodCall.createParameterMock(index, parseConstraint(methodCall, constraint, byteReaders));
        } else if (parameter.containsKey("methods")) { //i.e. object
            parseMockClass(methodCall.createParameterMock(index), methodCall, parameter, byteReaders);
        } else { //i.e. primitive no constraint
            ByteReader byteReader = ByteReader.createDefault();
            byteReaders.add(byteReader);
            methodCall.createParameterMock(index, byteReader);
        }
    }

    private static void parseParameters(MethodCall methodCall, JSONArray parameters,
            List<ByteReader> byteReaders) throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
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

    private static MethodCall createMethodCall(JSONObject methodCallObject) throws
            ClassNotFoundException,
            NoSuchMethodException {
        checkKeys(methodCallObject, "class", "method", "parameters");
        Class<?> mockClass = getClassForString((String) methodCallObject.get("class"));
        String methodName = (String) methodCallObject.get("method");
        JSONArray parameters = (JSONArray) methodCallObject.get("parameters");
        Class<?>[] parameterClasses = new Class[parameters.size()];
        for (int i = 0; i < parameterClasses.length; i++) {
            JSONObject object = (JSONObject) parameters.get(i);
            parameterClasses[i] = getClassForString((String) object.get("class"));
        }
        LOGGER.log(VERBOSITY,
                "Instrumented Class Path: " + mockClass.getProtectionDomain().getCodeSource().getLocation().getPath());
        return MethodCall.createMethodCall(mockClass, methodName, parameterClasses);
    }

    public static MethodCall setupMethodCall(Logger logger, JSONObject definition, List<ByteReader> byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        LOGGER.setParent(logger);
        LOGGER.log(VERBOSITY, "Started Parsing of Method Call");
        MethodCall methodCall = createMethodCall(definition);
        parseParameters(methodCall, (JSONArray) definition.get("parameters"), byteReaders);
        parseInstanceVariables(methodCall.getMethodMockClass(), methodCall,
                (JSONArray) definition.get("instance_variables"), byteReaders);
        LOGGER.log(VERBOSITY, "Created Method Call " + methodCall);
        return methodCall;
    }

    public static class MethodCallConfigKeyException extends RuntimeException {
        public MethodCallConfigKeyException(String key, JSONObject object) {
            super("Key " + key + " not found in " + object.toJSONString());
        }
    }


}
