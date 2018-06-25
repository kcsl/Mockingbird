package io;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

/**
 * @author Derrick Lockwood
 * @created 6/12/18.
 */
public class DataChunk implements DataInput {

    private final byte[] bytes;
    private int index;
    private int size;

    DataChunk(byte[] bytes, int sizeRead) {
        this.bytes = bytes;
        this.size = sizeRead;
    }

    public boolean isDoneReading() {
        return index >= size;
    }

    @Override
    public void readFully(byte[] b) {
        System.arraycopy(bytes, index, b, 0, b.length <= size ? b.length : size);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        System.arraycopy(bytes, index, b, off, len - off <= size ? len : size);
    }

    @Override
    public int skipBytes(int n) {
        int i = n + index;
        if (i >= size) {
            int tmp = index;
            index = size;
            return size - tmp;
        }
        index = i;
        return n;
    }

    private byte readPrimitiveByte() throws IOException {
        if (index >= size) {
            throw new EOFException();
        }
        byte b = bytes[index];
        index++;
        return b;
    }

    private void unreadPrimitiveByte() throws IOException {
        if (index <= 0) {
            throw new IOException();
        }
        index--;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return (readPrimitiveByte() != 0);
    }

    @Override
    public byte readByte() throws IOException {
        return readPrimitiveByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readPrimitiveByte();
    }

    @Override
    public short readShort() throws IOException {
        byte ch1 = readPrimitiveByte();
        byte ch2 = readPrimitiveByte();
        return (short) ((ch1 << 8) + (ch2));
    }

    @Override
    public int readUnsignedShort() throws IOException {
        byte ch1 = readPrimitiveByte();
        byte ch2 = readPrimitiveByte();
        return ((ch1 << 8) + (ch2));
    }

    @Override
    public char readChar() throws IOException {
        byte ch1 = readPrimitiveByte();
        byte ch2 = readPrimitiveByte();
        return (char) ((ch1 << 8) + (ch2));
    }

    @Override
    public int readInt() throws IOException {
        byte ch1 = readPrimitiveByte();
        byte ch2 = readPrimitiveByte();
        byte ch3 = readPrimitiveByte();
        byte ch4 = readPrimitiveByte();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    @Override
    public long readLong() throws IOException {
        byte[] readBuffer = new byte[8];
        for (int i = 0; i < readBuffer.length; i++) {
            readBuffer[i] = readPrimitiveByte();
        }
        return (((long) readBuffer[0] << 56) +
                ((long) (readBuffer[1] & 255) << 48) +
                ((long) (readBuffer[2] & 255) << 40) +
                ((long) (readBuffer[3] & 255) << 32) +
                ((long) (readBuffer[4] & 255) << 24) +
                ((readBuffer[5] & 255) << 16) +
                ((readBuffer[6] & 255) << 8) +
                ((readBuffer[7] & 255)));
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        byte c;

        loop:
        while (true) {
            switch (c = readPrimitiveByte()) {
                case -1:
                case '\n':
                    break loop;

                case '\r':
                    int c2 = readPrimitiveByte();
                    if ((c2 != '\n') && (c2 != -1)) {
                        unreadPrimitiveByte();
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

    @Override
    public String readUTF() throws IOException {
        return readLine();
    }
}
