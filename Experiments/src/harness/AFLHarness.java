package harness;

/**
 * @author Derrick Lockwood
 * @created 6/8/18.
 */
public class AFLHarness {

    public static void main(String[] args) {
//        String fileName = args[0];
//        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
//        MethodCall methodCall = MethodCall.createMethodCall(Foo.class, "function", int.class);
//        methodCall.linkMethodCallback(PrintMethodCallback.create());
//        methodCall.createParameterMock(0, ByteReader.createDefault(dataInputStream));
//        methodCall.run();

        /*
        MethodCall.createMethodCall(Class.forName(args[0]), args[1]);
        Is there a way to have a persistant state of the mocked objects instead of having to recreate every time
        we want to change a value from the AFL Fuzzer.
        Is there a way to read createObject a file that has static pieces that don't change like a config file and that way we can
        know which pieces that are changed go to which change.
        */
        //        ByteReader width = new ByteReader(bufferedReader, 2);
//        ByteReader height = new ByteReader(bufferedReader, 2);
//        ByteReader getRGB = new ByteReader(bufferedReader, 3);
//        MethodCall methodCall = MethodCall.createMethodCall(Intensify.class, "filter", BufferedImage.class);
//        methodCall.createParameterMock(0)
//                .applyMethod(width, "getWidth")
//                .applyMethod(height, "getHeight")
//                .applyMethod(getRGB, "getRGB", int.class, int.class)
//                .applyMethod("setRGB", int.class, int.class, int.class);
//        methodCall.linkMethodCallback(PrintMethodCallback.create().link(ExceptionMethodCallback.create()));
//        methodCall.run();
    }

}
