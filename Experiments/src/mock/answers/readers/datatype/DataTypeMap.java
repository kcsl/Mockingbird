package mock.answers.readers.datatype;

import java.io.DataInput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 8/30/18.
 */
public class DataTypeMap {
    private final Map<Class<?>, DataTypeFunction<?>> dataTypeMap;

    public DataTypeMap() {
        dataTypeMap = new HashMap<>();

        //Add Primitives
        DataTypeFunction<Byte> byteReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readByte();
        DataTypeFunction<Character> characterReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readChar();
        DataTypeFunction<Boolean> booleanReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readBoolean();
        DataTypeFunction<Short> shortReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readShort();
        DataTypeFunction<Integer> integerReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readInt();
        DataTypeFunction<Long> longReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readLong();
        DataTypeFunction<Float> floatReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readFloat();
        DataTypeFunction<Double> doubleReaderFunction = (dataInput, dataTypeMap, objects) -> dataInput.readDouble();

        set(byte.class, byteReaderFunction);
        set(Byte.class, byteReaderFunction);
        set(char.class, characterReaderFunction);
        set(Character.class, characterReaderFunction);
        set(boolean.class, booleanReaderFunction);
        set(Boolean.class, booleanReaderFunction);
        set(short.class, shortReaderFunction);
        set(Short.class, shortReaderFunction);
        set(int.class, integerReaderFunction);
        set(Integer.class, integerReaderFunction);
        set(long.class, longReaderFunction);
        set(Long.class, longReaderFunction);
        set(float.class, floatReaderFunction);
        set(Float.class, floatReaderFunction);
        set(double.class, doubleReaderFunction);
        set(Double.class, doubleReaderFunction);
        set(Void.class, (dataInput, dataTypeMap, objects) -> null);

        //Add More common classes
        //TODO: Possibly supply the correct Answers instead of Objects
        set(new DataTypeReader<String>(String.class, new Class[]{Integer.class}, new Class[]{byte[].class}) {
            @Override
            protected String read(DataInput dataInput, DataTypeMap map, Object... objects) throws IOException {
                byte[] bytes;
                if (objects[1] == null) {
                    bytes = new byte[((int) objects[0]) * 2];
                    dataInput.readFully(bytes);
                } else {
                    bytes = (byte[]) objects[1];
                }
                return new String(bytes);
            }
        });

        set(new DataTypeReader<BigInteger>(BigInteger.class, new Class[]{int.class}, new Class[]{byte[].class}) {
            @Override
            protected BigInteger read(DataInput dataInput, DataTypeMap map, Object... objects) throws IOException {
                byte[] bytes;
                if (objects[1] == null) {
                    bytes = new byte[(int) objects[0]];
                    dataInput.readFully(bytes);
                } else {
                    bytes = (byte[]) objects[1];
                }
                return new BigInteger(bytes);
            }
        });

        set(new DataTypeReader<BigDecimal>(BigDecimal.class, new Class[]{int.class, int.class}, new Class[]{byte[].class}) {
            @Override
            protected BigDecimal read(DataInput dataInput, DataTypeMap map, Object... objects) throws IOException {
                byte[] bytes;
                if (objects[2] == null) {
                    bytes = new byte[(int) objects[1]];
                    dataInput.readFully(bytes);
                } else {
                    bytes = (byte[]) objects[2];
                }
                return new BigDecimal(new BigInteger(bytes), (int) objects[0]);
            }
        });
    }

    public <T> void set(Class<T> type, DataTypeFunction<T> dataTypeFunction) {
        dataTypeMap.put(type, dataTypeFunction);
    }

    public <T> void set(DataTypeReader<T> dataTypeReader) {
        dataTypeMap.put(dataTypeReader.getType(), dataTypeReader);
    }

    @SuppressWarnings("unchecked")
    public <T> DataTypeFunction<T> getDataTypeFunction(Class<T> type) {
        DataTypeFunction<T> tDataTypeFunction = (DataTypeFunction<T>) dataTypeMap.get(type);
        if (tDataTypeFunction == null) {
            throw new TypeNotFoundException(type);
        }
        return tDataTypeFunction;
    }

    public boolean checkDataTypes(Class<?>... types) {
        for (Class<?> type : types) {
            if (!dataTypeMap.containsKey(type)) {
                return false;
            }
        }
        return true;
    }

    public static class TypeNotFoundException extends RuntimeException {
        public TypeNotFoundException(Class<?> type) {
            super(type.toString());
        }
    }

    public static class RequiredTypesNotSuppliedException extends RuntimeException {
        public RequiredTypesNotSuppliedException(Class<?>... requiredTypes) {
            super(Arrays.toString(requiredTypes));
        }
    }

}
