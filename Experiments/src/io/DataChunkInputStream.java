package io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Derrick Lockwood
 * @created 6/12/18.
 */
public class DataChunkInputStream extends DataInputStream {

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public DataChunkInputStream(InputStream in) {
        super(in);
    }

    public DataChunk readDataChunk(int chunkSize) throws IOException {
        byte[] bytes = new byte[chunkSize];
        int size = read(bytes);
        return new DataChunk(bytes, size);
    }

}
