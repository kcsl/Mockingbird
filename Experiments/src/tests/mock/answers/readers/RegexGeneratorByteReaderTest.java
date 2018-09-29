package mock.answers.readers;

import mock.answers.readers.regexreader.RegexGeneratorByteReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.BaseTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Derrick Lockwood
 * @created 8/2/18.
 */
public class RegexGeneratorByteReaderTest extends BaseTest {
    private File tmpFile;

    public RegexGeneratorByteReaderTest() {
        super(RegexGeneratorByteReader.class);
    }

    @Before
    public void before() {
        tmpFile = createTmpFile("").toFile();
    }

    @Test
    public void testReadDigit() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x80));
        Assert.assertEquals("Digit: 0", runTest("Digit: \\d"));
    }

    @Test
    public void testReadPlus() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x00, 0x00, 0x00, 0x03, 0x80, 0xA0, 0xBB));
        Assert.assertEquals("Digits: 012", runTest("Digits: \\d+"));
    }

    @Test
    public void testReadPlusEdge() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x00, 0x00, 0x00, 0x00, 0xFF));
        Assert.assertEquals("Digits: 4", runTest("Digits: \\d+"));
    }

    @Test
    public void testReadStar() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x00, 0x00, 0x00, 0x03, 0x80, 0xA0, 0xBB));
        Assert.assertEquals("Digits: 012", runTest("Digits: \\d*"));
    }

    @Test
    public void testReadStarEdge() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x00, 0x00, 0x00, 0x00));
        Assert.assertEquals("Digits: ", runTest("Digits: \\d*"));
    }

    @Test
    public void testReadCharacterClass() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x80));
        Assert.assertEquals("a or e: a", runTest("a or e: [ae]"));
        addBytes(tmpFile, convertHex(0x7F));
        Assert.assertEquals("a or e: e", runTest("a or e: [ae]"));
    }

    @Test
    public void testReadCharacterClassMultiple() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x00, 0x00, 0x00, 0x03, 0x80, 0x7F, 0x80));
        Assert.assertEquals("a or e: aea", runTest("a or e: [ae]+"));
    }

    @Test
    public void testReadCharacterNotClass() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0xAF));
        Assert.assertEquals("Not a or e: .", runTest("Not a or e: [^ae]"));
    }

    @Test
    public void testReadCharacterRangeClass() throws IOException {
        clearFile(tmpFile);
        addBytes(tmpFile, convertHex(0x80));
        Assert.assertEquals("Between a or e: a", runTest("Between a or e: [a-e]"));
        addBytes(tmpFile, convertHex(0x7F));
        Assert.assertEquals("Between a or e: e", runTest("Between a or e: [a-e]"));
        addBytes(tmpFile, convertHex(0x00));
        Assert.assertEquals("Between a or e: c", runTest("Between a or e: [a-e]"));
    }

    private String runTest(String regex) throws IOException {
        RegexGeneratorByteReader byteReader = new RegexGeneratorByteReader("TestByteReader", regex);
        byteReader.setInputStream(new FileInputStream(tmpFile));
        String s = (String) byteReader.applyReturnType(String.class, false);
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