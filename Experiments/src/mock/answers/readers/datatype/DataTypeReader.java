package mock.answers.readers.datatype;

import mock.answers.BasicAnswer;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Derrick Lockwood
 * @created 9/3/18.
 */
public abstract class DataTypeReader<T> implements DataTypeFunction<T> {

    private final Class<T> type;
    private final Class<?>[] readTypes;
    private final Class<?>[] optionalTypes;

    public DataTypeReader(Class<T> type) {
        this(type, null, null);
    }

    public DataTypeReader(Class<T> type, Class<?>[] readTypes, Class<?>[] optionalTypes) {
        this.type = type;
        this.readTypes = readTypes;
        this.optionalTypes = optionalTypes;
    }

    public Class<T> getType() {
        return type;
    }


    protected abstract T read(DataInput dataInput, DataTypeMap map, Object... objects) throws IOException;

    //Objects has to be supplied as the following [ readTypes..., optionalTypes..., typeObject].
    //readTypes is the only parameter that is necessary to have supplied if not they are read in.
    //optionalTypes can be null or not there which allows the read function to do its own processing
    //typeObject can be null or not there which is just the direct object type and is checked for at the end of the objects array

    //TODO: Make it such that objects are created by Answer Types
    @SuppressWarnings("unchecked")
    @Override
    public T apply(DataInput dataInput, DataTypeMap map, BasicAnswer... objects) throws IOException {
        int readTypesSize = (readTypes != null ? readTypes.length : 0);
        int optionalTypesSize = (optionalTypes != null ? optionalTypes.length : 0);
        if (objects != null &&
                objects.length > readTypesSize + optionalTypesSize &&
                objects[objects.length - 1] != null &&
                objects[objects.length - 1].getClass().isAssignableFrom(type)) {
            return (T) objects[objects.length - 1];
        }
        Object[] readInstances = new Object[readTypesSize + optionalTypesSize];
        for (int i = 0; readTypes != null && i < readTypes.length; i++) {
            if (objects == null ||
                    i >= objects.length ||
                    objects[i] == null) {
                //TODO: fix passedAnswers depending on which readTypes are necessary, (depends on i)
                BasicAnswer[] passedAnswers = objects == null ? null : Arrays.copyOfRange(objects,
                        Math.min(readInstances.length, objects.length), objects.length);
                readInstances[i] = map.getDataTypeFunction(readTypes[i]).apply(dataInput, map, passedAnswers);
            } else {
                Object o = objects[i].apply(null, null, readTypes[i]);
                if (o == null) {
                    BasicAnswer[] passedAnswers = Arrays.copyOfRange(objects,
                            Math.min(readInstances.length, objects.length), objects.length);
                    readInstances[i] = map.getDataTypeFunction(readTypes[i]).apply(dataInput, map, passedAnswers);
                    continue;
                }
                Class<?> clazz = o.getClass();
                if (!checkAssignable(readTypes[i], clazz, o)) {
                    //TODO: Fix the readTypes isAssignableFrom since its broken with long read from json and integers
                    throw new RuntimeException(
                            "Applied answer doesn't get read Type " + readTypes[i] + " vs. " + o.getClass());
                } else {
                    readInstances[i] = o;
                }
            }
        }
        for (int i = readTypesSize; optionalTypes != null && i < optionalTypes.length; i++) {
            if (objects != null &&
                    i < objects.length &&
                    objects[i] != null && optionalTypes[i].isAssignableFrom(objects[i].getClass())) {
                readInstances[i] = objects[i];
            }
        }
        return read(dataInput, map, readInstances);
    }

    private boolean checkAssignable(Class<?> type1, Class<?> type2, Object obj2) {
        if (type1 == null && type2 == null) {
            return true;
        } else if (type1 == null) {
            return false;
        } else if (type2 == null) {
            return false;
        }
        if (type1.isAssignableFrom(type2) || type2.isAssignableFrom(type1)) {
            return true;
        }
        try {
            type1.cast(obj2);
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
