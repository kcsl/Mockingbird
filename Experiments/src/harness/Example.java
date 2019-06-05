package harness;

import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 2019-03-25.
 */
public class Example {

    private int b;
    private String s;
    private Example e;

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

    private int simpleObjectParameterExample(Example example) {
        return example.b / example.s.length();
    }

    private int complexObjectParameterExample(Example example) {
        if (this.simpleParameterExample(3) > 4) {
            return 3;
        }
        return example.simpleParameterExample(2);
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

    /**
     *
     * Artifacts:
     *
     * Def Use chains of parameters, field variables, and object instance (this) with respect to method M
     *
     * Desired Information:
     *
     * Determine where parameters and field variables of the declared class C of M Use Def chains with respect to M.
     * This can include the following
     *  - primitives
     *    - mathematical operations done
     *    - conditional statements
     *  - object O
     *    - field variable F used in M contained in O
     *       - field variable F' of F used in M contained in F... etc.
     *    - method M' called on O
     *       - M' should be analyzed similar to M if it is contained in the code segment
     *       - Otherwise, M' is noted to be stubbed
     *
     */

    private int fofFExample() {
        if (e.e.b > 5) {
            return b;
        }
        return e.b;
    }

    /**
     * In this example, note two things
     *    - method simpleParameterExample needs to be analyzed or stubbed
     *    - method for object e.simpleParameterExample needs to be analyzed or stubbed
     */
    private int mofFExample() {
        if (simpleParameterExample(2) < 5) {
            return b;
        }
        return e.simpleParameterExample(2);
    }

    private int mixofAllExample(Example ex) {
        return ex.simpleParameterExample(2) + e.simpleParameterExample(3);
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

    /**
     * Desired Information:
     *
     * Determining if method M is static so the object O that calls M ( O.M(...) ) can be constructed
     */
    public static int staticParameterExample(int a, int b, int c) {
        if (a > 3) {
            return b / c;
        }
        return a + b;
    }


    private long cutExample(Foo foo) {
        if (b * foo.value < 0) {

            //We already have some analysis as to what values we get from stupidPow.
            //Now we want to test what values of b and foo.value create the vulnerability.

            return b / foo.stupidPow(b, foo.value) - 1;
        }
        StringBuilder sb = new StringBuilder();
        //Extra method doesn't pertain to the located vulnerability (we can just remove it?)
        modifier(sb);
        return sb.length() + b;
    }

    private long staticExample(int a, int c) {
        if (a < c) {
            return c / staticParameterExample(a, b, c);
        }
        return a + c;
    }

    private long fieldExample(Foo foo) {
        if (foo.value < 3) {
            return b / foo.value;
        }
        return foo.value;
    }



}
