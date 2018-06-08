package method.callbacks;

import method.MethodData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Derrick Lockwood
 * @created 6/8/18.
 */
public class CSVMethodCallback implements MethodCallback {
    private final BufferedWriter bufferedWriter;
    private final boolean flushImmediately;
    private CSVRule csvRule;

    private CSVMethodCallback(String filename, CSVRule csvRule, boolean flushImmediately) throws IOException {
        bufferedWriter = new BufferedWriter(new FileWriter(filename));
        this.csvRule = csvRule;
        this.flushImmediately = flushImmediately;
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {
        try {
            Object[] output;
            if (csvRule == null) {
                output = new Object[] {
                        methodData.getDeclaringClass(),
                        methodData.getMethodName(),
                        Arrays.toString(methodData.getParameterTypes()),
                        methodData.getDuration(),
                        methodData.getReturnValue(),
                        methodData.getReturnException(),
                        methodData.getDeltaHeapMemory()
                };
            } else {
                output = csvRule.getOutput(methodData);
            }
            if (output != null) {
                for (int i = 0; i < output.length - 1; i++) {
                    bufferedWriter.write(Objects.toString(output[i]));
                    bufferedWriter.write(',');
                }
                bufferedWriter.write(Objects.toString(output[output.length - 1]));
                bufferedWriter.write('\n');
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

    public static MethodCallback createWithImmediateWrite(String fileName, CSVRule csvRule) throws IOException {
        return new CSVMethodCallback(fileName, csvRule, true);
    }

    public static MethodCallback create(String fileName, CSVRule csvRule) throws IOException {
        return new CSVMethodCallback(fileName, csvRule, false);
    }

    public static MethodCallback create(String fileName) throws IOException {
        return create(fileName, null);
    }

    public interface CSVRule {
        Object[] getOutput(MethodData methodData);
    }

}
