package mock.answers.readers.inputstream;

import mock.answers.readers.regexreader.RegexGeneratorByteReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.BaseTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Derrick Lockwood
 * @created 9/4/18.
 */
public class ArrayByteReaderInputStreamTest extends BaseTest {

    private File tmpFile;

    public ArrayByteReaderInputStreamTest() {
        super(ArrayByteReaderInputStream.class);
    }

    @Before
    public void before() {
        tmpFile = createTmpFile("").toFile();
    }

    @Test
    public void testByteArray() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x00, 0x00, 0x00, 0x02, 0x56, 0x33));
        Assert.assertArrayEquals(convertHex(0x56, 0x33), runTest(byte[].class));
    }

    private <T> T runTest(Class<T> type) throws IOException {
        ArrayByteReaderInputStream arrayByteReaderInputStream = new ArrayByteReaderInputStream("Test");
        arrayByteReaderInputStream.setInputStream(new FileInputStream(tmpFile));
        return arrayByteReaderInputStream.readObject(type);
    }

    private static byte[] convertHex(int... hexes) {
        byte[] bytes = new byte[hexes.length];
        for (int i = 0; i < hexes.length; i++) {
            bytes[i] = (byte) hexes[i];
        }
        return bytes;
    }

}