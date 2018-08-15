package testagents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;

/**
 * @author Derrick Lockwood
 * @created 7/24/18.
 */
public class SpaceProcess {
    public static long getMemoryUsage(Function<String, Boolean> filter) throws IOException {
        Process process = Runtime.getRuntime().exec(new String[]{"ps", "-ef"});
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String pid = null;
        String s;
        while ((s = stdInput.readLine()) != null) {
            if (filter.apply(s)) {
                pid = s.trim().split("[ ]+")[1].trim();
                break;
            }
        }
        if (stdError.readLine() != null) {
            return 0;
        }
        if (pid != null) {
            process = Runtime.getRuntime().exec(new String[]{"ps", "-o", "rss", pid});
            stdInput.close();
            stdError.close();
            stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((s = stdInput.readLine()) != null) {
                try {
                    long l = Long.parseLong(s);
                    stdInput.close();
                    stdError.close();
                    return l;
                } catch (NumberFormatException ignored) {

                }
            }
            if (stdError.readLine() != null) {
                return 0;
            }
        }
        return 0;
    }
}
