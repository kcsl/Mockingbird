package mock.answers.readers.inputstream;

import mock.answers.BasicAnswer;
import mock.answers.readers.datatype.DataTypeMap;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Derrick Lockwood
 * @created 8/29/18.
 */
public class DefaultByteReaderInputStream extends ByteReaderInputStream {

    public DefaultByteReaderInputStream(String name, BasicAnswer... staticObjects) {
        super(name, staticObjects);
    }

    @Override
    public <T> T readObject(Class<T> type) throws IOException {
        try {
            return dataTypeMap.getDataTypeFunction(type).apply(this, dataTypeMap, staticObjects);
        } catch (DataTypeMap.TypeNotFoundException | DataTypeMap.RequiredTypesNotSuppliedException e) {
            throw new ClassNotReadException(name, type);
        }
    }

    @Override
    protected ByteReaderInputStream duplicateByteReader() {
        return new DefaultByteReaderInputStream(name, staticObjects);
    }

    @Override
    protected void handleReadException(IOException e) {
        throw new RuntimeException(e);
    }

}
