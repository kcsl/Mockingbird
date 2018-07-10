package instrumentor;

import org.junit.Assert;
import org.junit.Test;
import util.BaseTest;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Derrick Lockwood
 * @created 7/9/18.
 */
public class InstrumentLoaderTest extends BaseTest {

    public InstrumentLoaderTest() {
        super(InstrumentLoader.class);
    }

    @Test
    public void testGetClasses() {
        Path directory = createTmpDirectory();
        fillDirectory(directory, ".class", 10);
        fillDirectory(directory, ".test", 5);
        Path insideDir = addTempDirectory(directory);
        fillDirectory(insideDir, ".class", 2);
        fillDirectory(insideDir, ".test", 10);
        File[] classes = invokeStaticMethod(getMethod("getAllClasses", File.class), directory.toFile());
        Assert.assertEquals(12, classes.length);
        for (File aClass : classes) {
            Assert.assertTrue(aClass.getName().endsWith(".class"));
        }
    }
}