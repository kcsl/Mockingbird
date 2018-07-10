package io;

import java.io.*;

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

    public DataChunkInputStream readDataChunk(int chunkSize) throws IOException {
        byte[] bytes = new byte[chunkSize];
        int size = read(bytes);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        return new DataChunkInputStream(byteArrayInputStream);
    }

    public String readToNewLine() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        byte c;
        loop:
        while (true) {
            switch (c = readByte()) {
                case -1:
                case '\n':
                    break loop;

                case '\r':
                    int c2 = readByte();
                    if ((c2 != '\n') && (c2 != -1)) {
                        if (!(in instanceof PushbackInputStream)) {
                            this.in = new PushbackInputStream(in);
                        }
                        ((PushbackInputStream) in).unread(c2);
                    }
                    break loop;

                default:
                    stringBuilder.append((char) c);
            }
        }
        if ((c == -1)) {
            return null;
        }
        return stringBuilder.toString();
    }
}
