package mock.answers;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 8/10/18.
 */
public class CSVOutputAnswer implements Answer {

    private final PrintStream printStream;

    public CSVOutputAnswer(File file) throws FileNotFoundException {
        printStream = new PrintStream(new FileOutputStream(file, false));
    }

    private CSVOutputAnswer(PrintStream printStream) {
        this.printStream = printStream;
    }

    private void printObjects(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append('"');
            if (object != null && object.getClass().isArray()) {
                stringBuilder.append(Arrays.toString((Object[]) object));
            } else {
                stringBuilder.append(Objects.toString(object));
            }
            stringBuilder.append('"').append(',');
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        printStream.println(stringBuilder);
    }

    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        printObjects(proxy, parameters, name, returnType);
        return null;
    }

    @Override
    public Object handle(Object proxy, Object[] args, Method method) throws Throwable {
        printObjects(proxy, args, method);
        return null;
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) throws Throwable {
        printObjects(proxy, args, method);
        return null;
    }

    @Override
    public Object handle(Object[] args) throws Throwable {
        printObjects(args);
        return null;
    }

    @Override
    public Answer duplicate() {
        return new CSVOutputAnswer(printStream);
    }
}
