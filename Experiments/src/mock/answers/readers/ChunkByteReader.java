package mock.answers.readers;

import java.io.DataInput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Supports the classes of String, BigInteger, BigDecimal
 *
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class ChunkByteReader extends ByteReader {

    public ChunkByteReader(int chunkSize) {
        super(null, chunkSize);
    }

    @Override
    ByteReader duplicateByteReader() {
        return new ChunkByteReader(chunkSize);
    }

    @Override
    void handleReadException(IOException e) {
        throw new RuntimeException(e.getCause());
    }

    @Override
    Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) throws IOException {
        if (returnType.isAssignableFrom(String.class)) {
            byte[] bytes = new byte[chunkSize];
            dataInput.readFully(bytes);
            return new String(bytes);
        } else if (returnType.isAssignableFrom(BigInteger.class)) {
            byte[] bytes = new byte[chunkSize];
            dataInput.readFully(bytes);
            return new BigInteger(bytes);
        } else if (returnType.isAssignableFrom(BigDecimal.class)) {
            byte[] bytes = new byte[chunkSize];
            dataInput.readFully(bytes);
            int scale = dataInput.readInt();
            return new BigDecimal(new BigInteger(bytes), scale);
        }
        return null;
    }

    @Override
    Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }

}
