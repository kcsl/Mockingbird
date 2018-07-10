package util;


import org.junit.Assert;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Derrick Lockwood
 * @created 7/9/18.
 */
public class BaseTest {
    private final Class<?> initClass;

    public BaseTest(Class<?> initClass) {
        this.initClass = initClass;
    }

    protected Method getMethod(String methodName, Class<?>... params) {
        Method method = null;
        try {
            method = initClass.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }
        method.setAccessible(true);
        return method;
    }

    @SuppressWarnings("unchecked")
    protected <T> T invokeStaticMethod(Method method, Object... params) {
        return invokeMethod(method, null, params);
    }

    @SuppressWarnings("unchecked")
    protected <T> T invokeMethod(Method method, Object object, Object... params) {
        try {
            return (T) method.invoke(object, params);
        } catch (IllegalAccessException e) {
            Assert.fail(e.getMessage());
        } catch (InvocationTargetException e) {
            Assert.fail("INSIDE METHOD: " + e.getCause().getMessage());
        }
        return null;
    }

    protected Path createTmpDirectory() {
        Path path = null;
        try {
            path = Files.createTempDirectory(initClass.getName());
            deletePathOnExit(path);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return path;
    }

    protected Path createTmpFile(Path dir, String suffix) {
        Path path = null;
        try {
            path = Files.createTempFile(dir, initClass.getName(), suffix);
            deletePathOnExit(path);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return path;
    }

    protected void addRandomBytes(File file, long numberOfBytes) {
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            for (long i = 0; i < numberOfBytes; i++) {
                bufferedOutputStream.write((int) (Math.random() * 256));
            }
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    protected void addString(File file, String s) {
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            bufferedOutputStream.write(s.getBytes());
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    protected Path createTmpFile(String suffix) {
        Path path = null;
        try {
            path = Files.createTempFile(initClass.getName(), suffix);
            deletePathOnExit(path);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return path;
    }

    protected Path addTempDirectory(Path directory) {
        try {
            return Files.createTempDirectory(directory, initClass.getName());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    protected Path[] fillDirectory(Path path, String suffix, int size) {
        Path[] paths = new Path[size];
        for (int i = 0; i < size; i++) {
            try {
                paths[i] = Files.createTempFile(path, initClass.getName(), suffix);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
        return paths;
    }

    private void deletePathOnExit(Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            private boolean deleteFile(File file) {
                if (!file.isDirectory()) {
                    return file.delete();
                } else {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (!deleteFile(f)) {
                                return false;
                            }
                        }
                    }
                    return file.delete();
                }
            }

            @Override
            public void run() {
                File directory = path.toFile();
                if (!deleteFile(directory)) {
                    throw new RuntimeException("Unable to remove temporary directory " + path);
                }
            }
        }));
    }

}
