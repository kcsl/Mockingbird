package mock.answers.readers.inputstream;

import io.DataChunkInputStream;
import mock.answers.BasicAnswer;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Derrick Lockwood
 * @created 8/29/18.
 */
public class ChunkByteReaderInputStream extends DefaultByteReaderInputStream {

    private int size;

    public ChunkByteReaderInputStream(String name, BasicAnswer... staticObjects) {
        super(name, staticObjects);
        this.size = -1;
    }

    public ChunkByteReaderInputStream(String name, int size, BasicAnswer... staticObjects) {
        super(name, staticObjects);
        if (size <= 0) {
            size = 1;
        }
        this.size = size;
    }


    @Override
    public <T> T readObject(Class<T> type) throws IOException {
        if (size == -1) {
            size = Math.abs(readInt());
        }

        byte[] bytes = new byte[size];
        int readSize = read(bytes);
        if (readSize < bytes.length) {
            //TODO: Do something when it can't read chunk?
            throw new RuntimeException("Byte Chunk Not read in " + name);
        }
        setInputStream(new ByteArrayInputStream(bytes));
        return super.readObject(type);
    }

    @Override
    protected ByteReaderInputStream duplicateByteReader() {
        return new ChunkByteReaderInputStream(name, size, staticObjects);
    }
}
