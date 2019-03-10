package method;

import mock.ClassMap;
import mock.ConstructAnswer;
import mock.TransformClassLoader;
import mock.answers.*;
import mock.answers.readers.inputstream.ByteReaderInputStream;
import mock.answers.readers.inputstream.ByteReaderInputStreamList;
import mock.answers.readers.inputstream.DefaultByteReaderInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
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

    private static ConstructAnswer parseConstruct(String readerName, JSONObject construct,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            NoSuchFieldException,
            ClassNotFoundException {
        LOGGER.log(Level.INFO, "Parsing Construct");
        ConstructAnswer answer;
        if (!construct.containsKey("params")) {
            answer = new ConstructAnswer(null, null);
        } else {
            JSONObject jsonObject = (JSONObject) construct.get("params");
            Set<String> keys = jsonObject.keySet();

            Iterator iterator = keys.iterator();
            Answer[] answers = new Answer[keys.size()];
            for (int i = 0; i < answers.length; i++) {
                answers[i] = parseConstraint(readerName + ":" + i,
                        (JSONObject) jsonObject.get(iterator.next()), byteReaders);
            }
            answer = new ConstructAnswer(keys.toArray(new String[0]), answers);
        }
        LOGGER.log(Level.INFO, "End Parsing Construct");
        return answer;
    }

//    private static Answer parseConstraint(String readerName, MethodCallDEL methodCallDEL, JSONObject constraint,
//            ByteReaderInputStreamList byteReaders) throws
//            ClassNotFoundException,
//            NoSuchMethodException,
//            NoSuchFieldException {
//        if (constraint == null) {
//            ByteReaderInputStream byteReader = new DefaultByteReaderInputStream(readerName);
//            byteReaders.add(byteReader);
//            return byteReader;
//        }
//        checkKeys(constraint, "type");
//        String type = (String) constraint.get("type");
//        LOGGER.log(VERBOSITY, "Started Parsing Constraint type " + type);
//        Answer answer = null;
//        if (methodCallDEL != null) {
//            switch (type) {
//                case "class":
//                    if (constraint.containsKey("name")) {
//                        answer = new ObjectInstantiatorAnswer(methodCallDEL.getObjectInstantiatorByName(
//                                (String) constraint.get("name")));
//                    } else {
//                        answer = new ObjectInstantiatorAnswer(parseMockClass(methodCallDEL, constraint, byteReaders));
//                    }
//                    break;
//                case "method":
//                    String name = (String) constraint.get("name");
//                    String[] splits = name.split(".");
//                    ObjectInstantiator<?> objectInstantiator = methodCallDEL.getObjectInstantiatorByName(splits[0]);
//                    MockClassDELDEL mockClassDEL = objectInstantiator instanceof MockClassDELDEL ? (MockClassDELDEL) objectInstantiator : null;
//                    if (mockClassDEL != null) {
//                        String[] methodFeatures = splits[0].split("[(]");
//                        String methodName = methodFeatures[0];
//                        methodFeatures[1] = methodFeatures[1].replace(")", "");
//                        String[] parameters = methodFeatures[1].split(",");
//                        Class<?>[] params = new Class[parameters.length];
//                        for (int i = 0; i < parameters.length; i++) {
//                            params[i] = getClassForString(parameters[i]);
//                        }
//                        answer = mockClassDEL.getAnswer(methodName, params);
//                    } else {
//                        LOGGER.log(Level.WARNING,
//                                "Parsing of constraint object instantiator needs to be a MockClassDELDEL to get reference methods");
//                        answer = null;
//                    }
//                    break;
//            }
//        }
//        if (answer == null) {
//            switch (type) {
//                case "construct":
//                    answer = parseConstruct(readerName, methodCallDEL, constraint, byteReaders);
//                    break;
//                case "param":
//                    int index = (int) constraint.get("index");
//                    answer = new ParameterIndexAnswer(index);
//                    break;
//                case "void":
//                    answer = new EmptyAnswer();
//                    break;
//                case "fixed":
//                    answer = new FixedAnswer(constraint.get("value"));
//                    break;
//                case "original":
//                    answer = new OriginalMethodAnswer();
//                    break;
//                default:
//                    BasicAnswer[] staticObjects = null;
//                    if (constraint.containsKey("static")) {
//                        JSONArray array = (JSONArray) constraint.get("static");
//                        staticObjects = new BasicAnswer[array.size()];
//                        for (int i = 0; i < array.size(); i++) {
//                            Object object = array.get(i);
//                            if (object instanceof JSONObject) {
//                                staticObjects[i] = BasicAnswer.transform(parseConstraint(readerName + ":staticObj:" + i,
//                                        methodCallDEL,
//                                        (JSONObject) object, byteReaders));
//                            } else {
//                                if (object instanceof Long) {
//                                    object = Math.toIntExact((Long) object);
//                                }
//                                staticObjects[i] = new FixedAnswer(object);
//                            }
//
//                        }
//                    }
//                    ByteReaderInputStream byteReader = ByteReaderInputStream.createByType(readerName, type, constraint,
//                            staticObjects);
//                    byteReaders.add(byteReader);
//                    answer = byteReader;
//                    break;
//            }
//        }
//
//        if (constraint.containsKey("constraint") && answer != null) {
//            answer = answer.link(
//                    parseConstraint(readerName, methodCallDEL, (JSONObject) constraint.get("constraint"), byteReaders));
//        }
//        LOGGER.log(VERBOSITY, "Completed Parsing Constraint type " + type);
//        return answer;
//    }

    private static Answer parseConstraint(String readerName, JSONObject constraint,
            ByteReaderInputStreamList byteReaders) throws ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        String type = (String) constraint.get("type");
        Answer answer;
        switch (type) {
            case "construct":
                answer = parseConstruct(readerName, constraint, byteReaders);
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
                            staticObjects[i] = BasicAnswer.transform(
                                    parseConstraint(readerName + ":staticObj:" + i, (JSONObject) object,
                                            byteReaders));
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

        return answer;
    }

    private static void parseMockMethod(ClassMap classMap, JSONObject method,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        String name = (String) method.get("name");
        LOGGER.log(VERBOSITY, "Started Parsing Method " + name);
        String[] parameterClassesArray;
        if (method.containsKey("parameters")) {
            JSONArray parameterArray = (JSONArray) method.get("parameters");
            parameterClassesArray = new String[parameterArray.size()];
            for (int k = 0; k < parameterArray.size(); k++) {
                parameterClassesArray[k] = (String) parameterArray.get(k);
            }
        } else {
            parameterClassesArray = new String[0];
        }
        classMap.applyMethod(parseConstraint(name, (JSONObject) method.get("constraint"), byteReaders),
                name, parameterClassesArray);
        LOGGER.log(VERBOSITY, "Completed Parsing Method " + name + " | " + Arrays.toString(parameterClassesArray));
    }

    private static void parseMockMethods(ClassMap classMap, JSONArray methods,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            NoSuchFieldException,
            ClassNotFoundException {
        LOGGER.log(VERBOSITY, "Started Parsing Mock Methods");
        if (methods != null) {
            for (Object methodObj : methods) {
                JSONObject method = (JSONObject) methodObj;
                parseMockMethod(classMap, method, byteReaders);
            }
        } else {
            LOGGER.log(Level.CONFIG,
                    "No definition of methods supplied to class " + classMap.getAssociatedMockClass().getCanonicalName() + " Skipping...");
        }
        LOGGER.log(VERBOSITY, "Completed Parsing Mock Methods");
    }

    private static void parseInstanceVariable(ClassMap classMap, String name,
            JSONObject instanceVariable, ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Parsing of Instance Variable " + name);
        if (instanceVariable.containsKey("constraint")) { //i.e. primitive
            LOGGER.log(VERBOSITY, "Instance variable primitive");
            JSONObject constraint = (JSONObject) instanceVariable.get("constraint");
            classMap.applyField(name, parseConstraint(name, constraint, byteReaders));
        } else if (instanceVariable.containsKey("methods")) { //i.e. object
            LOGGER.log(VERBOSITY, "Instance variable object");
            //TODO: What is this?!?!?!
        } else { //i.e. primitive no constraint
            LOGGER.log(VERBOSITY, "Instance variable primitive no constraint");
            ByteReaderInputStream byteReader = new DefaultByteReaderInputStream(name);
            byteReaders.add(byteReader);
            classMap.applyField(name, byteReader);
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Instance Variable " + name);
    }

    private static void parseInstanceVariables(ClassMap classMap,
            JSONArray instanceVariables,
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
                parseInstanceVariable(classMap, name, instanceVariable, byteReaders);
            }
        } else {
            LOGGER.log(Level.CONFIG,
                    "No definition of Instance Variables supplied to class " + classMap.getAssociatedMockClass().getCanonicalName() + " Skipping...");
        }
        LOGGER.log(VERBOSITY, "Completed Parsing of Instance Variables");
    }

    private static void parseMockClass(ClassMap classMap, JSONObject mockClassObject,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Parsing of Mock Class " + classMap.getAssociatedMockClass().getCanonicalName());
        JSONArray jsonArray = (JSONArray) mockClassObject.get("methods");
        String name = (String) mockClassObject.get("name");
        if (name != null) {
            classMap.setName(name);
        }
        //TODO: name for classMap?
        parseMockMethods(classMap, jsonArray, byteReaders);

        jsonArray = (JSONArray) mockClassObject.get("instance_variables");
        parseInstanceVariables(classMap, jsonArray, byteReaders);
        LOGGER.log(VERBOSITY, "Completed Parsing of Mock Class " + classMap.getAssociatedMockClass().getCanonicalName());
    }

    private static void parseParameter(MethodCall methodCall, int index, JSONObject parameter,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException,
            NoSuchFieldException {
        String name = methodCall.getParameterTypeCanonicalName(index);
        LOGGER.log(VERBOSITY, "Started Parsing of Parameter " + name);
        ClassMap parameterClassMap = new ClassMap();
        if (methodCall.parameterIsPrimitive(index)) {
            JSONObject constraint = (JSONObject) parameter.get("constraint");
            parameterClassMap.setConstructAnswer(parseConstraint("parameter-" + index, constraint, byteReaders));
            LOGGER.log(VERBOSITY, "Parsed Primitive Parameter");
        } else {
            parseMockClass(parameterClassMap, parameter, byteReaders);
        }
        methodCall.associateClassMapToParameter(index, parameterClassMap);
        LOGGER.log(VERBOSITY, "Completed Parsing of Parameter " + parameterClassMap.getAssociatedMockClass().getCanonicalName());
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

    private static MethodCall createMethodCall(TransformClassLoader classLoader, JSONObject methodCallObject,
            ByteReaderInputStreamList byteReaders) throws
            ClassNotFoundException,
            NoSuchMethodException, NoSuchFieldException {
        LOGGER.log(VERBOSITY, "Started Creating Method Call");
        String mockClass = (String) methodCallObject.get("class");
        String methodName = (String) methodCallObject.get("method");
        ConstructAnswer constructParamAnswer = null;
        if (methodCallObject.containsKey("construct")) {
            constructParamAnswer = parseConstruct(mockClass,
                    (JSONObject) methodCallObject.get("construct"), byteReaders);
        }
        JSONArray parameters = (JSONArray) methodCallObject.get("parameters");
        String[] parameterTypes;
        if (parameters != null) {
            parameterTypes = new String[parameters.size()];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypes[i] = (String) parameters.get(i);
            }
        } else {
            parameterTypes = new String[0];
        }

        LOGGER.log(VERBOSITY,
                "Instrumented Class Path: " + mockClass);
        MethodCall methodCall = new MethodCall(classLoader, mockClass, methodName, parameterTypes);
        methodCall.constructMethodClass(constructParamAnswer);
        LOGGER.log(VERBOSITY, "Finished Creating Method Call");
        return methodCall;
    }

    public static MethodCall setupMethodCall(Logger logger, TransformClassLoader transformClassLoader,
            JSONObject definition,
            ByteReaderInputStreamList byteReaders) throws
            NoSuchMethodException,
            ClassNotFoundException,
            NoSuchFieldException {
        LOGGER.setParent(logger);
        LOGGER.log(VERBOSITY, "Started Parsing of Method Call");
        MethodCall methodCall = createMethodCall(transformClassLoader, definition, byteReaders);
        parseParameters(methodCall, (JSONArray) definition.get("parameters"), byteReaders);
        parseInstanceVariables(methodCall.getMethodClassMap(),
                (JSONArray) definition.get("instance_variables"), byteReaders);
        parseMockMethods(methodCall.getMethodClassMap(), (JSONArray) definition.get("methods"),
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
