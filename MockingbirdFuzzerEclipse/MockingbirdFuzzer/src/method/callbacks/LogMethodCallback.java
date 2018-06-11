package method.callbacks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import method.MethodData;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class LogMethodCallback implements MethodCallback {

    private final BufferedWriter bufferedWriter;
    private final boolean flushImmediately;
    private OutputRule logRule;

    private LogMethodCallback(String filename, OutputRule logRule, boolean flushImmediately) throws IOException {
        bufferedWriter = new BufferedWriter(new FileWriter(filename));
        this.logRule = logRule;
        this.flushImmediately = flushImmediately;
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {
        try {
            if (logRule == null) {
                bufferedWriter.write(methodData.toString());
            } else {
                String output = logRule.getOutput(methodData);
                if (output != null) {
                    bufferedWriter.write(output);
                }
            }
            if (flushImmediately) {
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndIteration() {
        try {
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean continueIteration() {
        return false;
    }

    public static MethodCallback createWithImmediateWrite(String fileName, OutputRule logRule) throws IOException {
        return new LogMethodCallback(fileName, logRule, true);
    }

    public static MethodCallback create(String fileName) throws IOException {
        return create(fileName, null);
    }

    public static MethodCallback create(String fileName, OutputRule logRule) throws IOException {
        return new LogMethodCallback(fileName, logRule, false);
    }

}
