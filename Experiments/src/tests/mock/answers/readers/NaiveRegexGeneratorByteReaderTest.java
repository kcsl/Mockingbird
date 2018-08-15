package mock.answers.readers;

import mock.answers.readers.regexreader.RegexGeneratorByteReader;
import org.junit.Assert;
import org.junit.Test;
import util.BaseTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author Derrick Lockwood
 * @created 8/8/18.
 */
public class NaiveRegexGeneratorByteReaderTest extends BaseTest {

    private File tmpFile;

    public NaiveRegexGeneratorByteReaderTest() {
        super(NaiveRegexGeneratorByteReader.class);
        tmpFile = createTmpFile("").toFile();
    }

    @Test
    public void testDigit() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertInteger(4));
        Assert.assertEquals("Hello Digit: 3", runTest("Hello Digit: \\d"));
    }

    @Test
    public void testDigitPlus() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertInteger(27));
        Assert.assertEquals("Hello Digit: 0000000015", runTest("Hello Digit: \\d{0,10}"));
    }

    private String runTest(String regex) throws IOException {
        NaiveRegexGeneratorByteReader byteReader = new NaiveRegexGeneratorByteReader("TestByteReader", regex);
        byteReader.setInputStream(new FileInputStream(tmpFile));
        String s = (String) byteReader.createObject(String.class, false);
        byteReader.closeInputStream();
        return s;
    }

    private static byte[] convertInteger(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private static byte[] convertHex(int... hexes) {
        byte[] bytes = new byte[hexes.length];
        for (int i = 0; i < hexes.length; i++) {
            bytes[i] = (byte) hexes[i];
        }
        return bytes;
    }
}