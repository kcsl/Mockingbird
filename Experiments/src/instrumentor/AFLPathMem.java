package instrumentor;

/**
 * @author Derrick Lockwood
 * @created 11/5/18.
 */
public class AFLPathMem {
    public static final int SIZE = 65536;
    public static byte mem[] = new byte[SIZE];
    public static int prev_location = 0;

    /**
     * Clears the current measurements.
     */
    public static void clear() {
        for (int i = 0; i < SIZE; i++)
            mem[i] = 0;
    }

    /**
     * Prints to stdout any cell that contains a non-zero value.
     */
    public static String print() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            if (mem[i] != 0) {
                stringBuilder.append(i).append(" -> ").append(mem[i]).append('\n');
            }
        }
        return stringBuilder.toString();
    }
}
