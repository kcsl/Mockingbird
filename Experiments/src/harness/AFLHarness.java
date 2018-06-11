package harness;

import com.stac.image.algorithms.filters.Intensify;
import method.MethodCall;
import method.callbacks.PrintMethodCallback;
import mock.answers.EmptyAnswer;
import mock.answers.SubAnswer;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/8/18.
 */
public class AFLHarness {

    public static void main(String[] args) throws NoSuchMethodException, IOException {
        String fileName = args[0];
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        ByteReader width = new ByteReader(bufferedReader, 2);
        ByteReader height = new ByteReader(bufferedReader, 2);
        ByteReader getRGB = new ByteReader(bufferedReader, 3);
        MethodCall methodCall = MethodCall.createMethodCall(Intensify.class, "filter", BufferedImage.class);
        methodCall.createParameterMock(0)
                .applyMethod(width, "getWidth")
                .applyMethod(height, "getHeight")
                .applyMethod(getRGB, "getRGB", int.class, int.class)
                .applyMethod( "setRGB", int.class, int.class, int.class);
        methodCall.linkMethodCallback(PrintMethodCallback.create());
        methodCall.run();
        /*
        MethodCall.createMethodCall(Class.forName(args[0]), args[1]);
        Is there a way to have a persistant state of the mocked objects instead of having to recreate every time
        we want to change a value from the AFL Fuzzer.
        Is there a way to read in a file that has static pieces that don't change like a config file and that way we can
        know which pieces that are changed go to which change.
        */
    }

    private static class ByteReader implements SubAnswer {

        private char[] bytes;
        private Object object;

        public ByteReader(BufferedReader bufferedReader, int bytesRead) throws IOException {
            bytes = new char[bytesRead];
            if (bufferedReader.read(bytes, 0, bytes.length) < bytesRead) {
                bytes = null;
            }
        }

        private Object initObject(Class<?> returnType) {
            if (object != null || bytes == null) {
                return object;
            }
            if (returnType.isAssignableFrom(int.class)) {
                int val = 0;
                for (int i = 0; i < bytes.length - 1; i++) {
                    val |= bytes[i];
                    val <<= 8;
                }
                val |= bytes[bytes.length - 1];
                val %= 256;
                object = val;
            } else {
                throw new RuntimeException("Object can't be serialized with type " + returnType.getName());
            }
            return object;
        }

        @Override
        public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) throws Throwable {
            return initObject(method.getReturnType());
        }

        @Override
        public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) throws Throwable {
            return initObject(returnType);
        }
    }

    interface Constraint {
        Object in(char[] bytes);
        char[] out(Object o);
    }

}
