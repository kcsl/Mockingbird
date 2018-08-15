package testagents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Derrick Lockwood
 * @created 7/23/18.
 */
public class CSVMethodMap {

    private Map<Method, Object[]> methodMap;
    private final int size;
    private final long offset;
    private RandomAccessFile outputStream;

    public CSVMethodMap(String fileName, String... headers) throws IOException {
        methodMap = new HashMap<>();
        if (headers != null) {
            this.size = headers.length;
        } else {
            this.size = 0;
        }
        File f = new File(fileName);
        if (f.exists()) {
            f.delete();
        }
        outputStream = new RandomAccessFile(f, "rw");
        outputStream.seek(0);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('"').append("Method Name").append('"').append(',');
        if (headers != null && headers.length > 0) {
            for (String header : headers) {
                stringBuilder.append('"').append(header).append('"').append(',');
            }
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append('\n');
            outputStream.write(stringBuilder.toString().getBytes());
        } else {
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append('\n');
            outputStream.write(stringBuilder.toString().getBytes());
        }
        offset = stringBuilder.length();
    }

    public void writeMethodMap() {
        try {
            outputStream.seek(offset);
            for (Map.Entry<Method, Object[]> entry : methodMap.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\"").append(entry.getKey().toString().replace("\"", "")).append("\",");
                for (Object o : entry.getValue()) {
                    stringBuilder.append('"')
                            .append(Objects.toString(o).replace("\"", ""))
                            .append('"')
                            .append(',');
                }
                stringBuilder.setLength(stringBuilder.length() - 1);
                stringBuilder.append('\n');
                outputStream.write(stringBuilder.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> void computeObject(Method method, int index, Function<T, T> compute) {
        Object[] data = methodMap.computeIfAbsent(method, k -> new Object[size]);
        data[index] = compute.apply((T) data[index]);
    }

    public <T> void putObject(Method method, int index, T t) {
        Object[] data = methodMap.computeIfAbsent(method, k -> new Object[size]);
        data[index] = t;
    }

    public void addPlaceholder(Method method) {
        methodMap.computeIfAbsent(method, k -> new Object[size]);
    }
}
