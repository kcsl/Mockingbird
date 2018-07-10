package io;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import util.BaseTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Derrick Lockwood
 * @created 7/9/18.
 */
public class DataChunkInputStreamTest extends BaseTest {
    private static File tempFile;
    private static DataChunkInputStream dataChunkInputStream;

    public DataChunkInputStreamTest() {
        super(DataChunkInputStream.class);
    }

    @BeforeClass
    public static void setUp() throws IOException {
        tempFile = Files.createTempFile(DataChunkInputStream.class.getName(), ".bin").toFile();
        dataChunkInputStream = new DataChunkInputStream(new FileInputStream(tempFile));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        dataChunkInputStream.close();
    }

    @Test
    public void testReadDataChunk() {
        String s = "\0\0\0\3\0\0\0\0\0\0\1\0" + "STACISCOOL\n" + "\115\0\104\0";
        addString(tempFile, s);
        try {
            DataChunkInputStream dataChunk = dataChunkInputStream.readDataChunk(s.length());
            Assert.assertEquals(3, dataChunk.readInt());
            Assert.assertEquals(256, dataChunk.readLong());
            Assert.assertEquals("STACISCOOL", dataChunk.readToNewLine());
            Assert.assertEquals(77, dataChunk.readByte());
            Assert.assertEquals('D', dataChunk.readChar());
            Assert.assertFalse(dataChunk.readBoolean());
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }
}
