package util;

import java.io.PrintStream;

/**
 * @author Derrick Lockwood
 * @created 8/7/18.
 */
public class ConsoleOut {

    private static final PrintStream out = System.out;

    public static void print(String s) {
        PrintStream tmp = System.out;
        System.setOut(out);
        System.out.print(s);
        System.setOut(tmp);
    }

    public static void println(String s) {
        print(s + "\n");
    }

}
