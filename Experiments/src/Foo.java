import java.util.HashMap;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Foo {

    public int value;
    public Map<String, String> map = new HashMap<>();

    public static String staticTest() {
        return "Hello";
    }

    public int test(Foo param) {
        return -1;
    }

    public String strTest() {
        return "Hello World";
    }

    public int test1(int param) {
        return -1;
    }

    public Object test2(int param) {
        return null;
    }

    public int loops(Foo a, Foo b) {
        int total = 0;
        for (int i = 0; i < a.test(b); i++) {
            for (int j = 0; j < b.test(a); j++) {
                total += 1;
            }
        }
        return total;
    }

    public void spaceTest() {
        int size = 1000;
        for (int j = 0; j < size; j++) {
            for(int i = 0; i < size; i++) {
                map.put("test-"+Math.random(), "Value-"+Math.random());
            }
        }
    }

    public void function(int x) throws InterruptedException {
        double N = 10000000005.0;
        double z = 0;
        for(int i = 0; i<x; i++){ // z = N*x
            z+=N;
        }
        double w = z/x; // w = z/x = N*x/x = N
        if((long)Math.abs(N - w) != 0){
            // Do computationally expensive calculation
            // Shouldn't happen since w == N
            Thread.sleep(1000);
        }
    }
}
