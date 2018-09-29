package mock.answers.readers.inputstream;

import mock.answers.BasicAnswer;

import java.io.IOException;
import java.lang.reflect.Array;

/**
 * @author Derrick Lockwood
 * @created 8/30/18.
 */
public class ArrayByteReaderInputStream extends ByteReaderInputStream {

    private int size;

    public ArrayByteReaderInputStream(String name, BasicAnswer... staticObjects) {
        super(name, staticObjects);
        //TODO: support dimensions
        this.size = -1;
    }

    public ArrayByteReaderInputStream(String name, int size, BasicAnswer... staticObjects) {
        super(name, staticObjects);
        if (size <= 0) {
            size = 1;
        }
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readObject(Class<T> type) throws IOException {
        if (!type.isArray()) {
            throw new ClassNotReadException(name, type);
        }
        if (size == -1) {
            size = readInt();
        }
        Object arr = Array.newInstance(type.getComponentType(), size);
        for(int i = 0; i < size; i++) {
            Array.set(arr, i, dataTypeMap.getDataTypeFunction(type.getComponentType()).apply(this, dataTypeMap, staticObjects));
        }
        return (T) arr;
    }

    @Override
    protected ByteReaderInputStream duplicateByteReader() {
        return new ArrayByteReaderInputStream(name, size, staticObjects);
    }

    @Override
    protected void handleReadException(IOException e) {
        throw new RuntimeException(e);
    }
}
