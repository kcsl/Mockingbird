package mock.answers.readers.regexreader;

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
 * @created 8/5/18.
 */
public class RegexTokenizerByteReaderTest extends BaseTest {

    private File tmpFile;

    public RegexTokenizerByteReaderTest() {
        super(RegexGeneratorByteReader.class);
    }

    @Before
    public void before() {
        tmpFile = createTmpFile("").toFile();
    }

    @Test
    public void testRegexString() throws IOException {
        clearFile(tmpFile);
        Assert.assertEquals("Hello World", runTest("Hello World"));
    }

    @Test
    public void testRegexDigit() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x80));
        Assert.assertEquals("Hello World 0", runTest("Hello World \\d"));
    }

    @Test
    public void testRegexDigitPlus() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x00, 0x00, 0x00, 0x02, 0x80, 0x00));
        Assert.assertEquals("Hello World 04", runTest("Hello World \\d+"));
    }

    @Test
    public void testRegexDigitRange() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x7F, 0xFF, 0xFF, 0xFF, 0x80, 0x00, 0x80));
        Assert.assertEquals("Hello World 040", runTest("Hello World \\d{2,3}"));
    }

    @Test
    public void testRegexCharacterClassLetters() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x80));
        Assert.assertEquals("Fun Stuff a and after", runTest("Fun Stuff [ae] and after"));
    }

    private String runTest(String regex) throws IOException {
        RegexTokenizerByteReader byteReader = new RegexTokenizerByteReader("TestByteReader", regex);
        byteReader.setInputStream(new FileInputStream(tmpFile));
        String s = (String) byteReader.createObject(String.class, false);
        byteReader.closeInputStream();
        return s;
    }

    private static byte[] convertHex(int... hexes) {
        byte[] bytes = new byte[hexes.length];
        for (int i = 0; i < hexes.length; i++) {
            bytes[i] = (byte) hexes[i];
        }
        return bytes;
    }

    private static void printBytes(byte... bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            stringBuilder.append(String.format("%02X", aByte)).append(' ');
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        System.out.println(stringBuilder);
    }

}