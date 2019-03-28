package harness;

/**
 * @author Derrick Lockwood
 * @created 2019-03-25.
 */
public class Example {

    private int b;
    private String s;

    public Example(String s, int b) {
        //Bad Constructor does some weird operations
        for (int i = 0; i < b; i++) {
            try {
                Thread.sleep(s.length() * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.s = s;
        this.b = b;
    }

    private int simpleParameterExample(int a) {
        if (a == 2) {
            return a / b;
        }
        return s.length();
    }

    public int methodCallExample(int a, int b, int c) {
        int k = a + b;
        return c / badMethod(k);
    }

    private int badMethod(int t) {
        if (t == 8) {
            return 0;
        }
        try {
            Thread.sleep(Math.abs(t) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return t / 2;
    }

    private int fieldVariableExample(int a, int c) {
        if (b * a < 0) {
            return b / c;
        }
        return b;
    }

    private String stringFieldVariableExample() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < this.s.length(); i++) {
            if ("aeiou".indexOf(this.s.charAt(i)) >= 0) {
                s.append(this.s.charAt(i));
            }
        }
        return s.toString();
    }

    private int objectModificationExample(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bad Methods");
        modifier(sb);
        if (s.length() > sb.length()) {
            return s.length() / sb.length();
        }
        return sb.length() / s.length();
    }

    private void modifier(StringBuilder s) {
        s.setLength(0);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int staticParameterExample(int a, int b, int c) {
        if (a > 3) {
            return b / c;
        }
        return a + b;
    }

}
