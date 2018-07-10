package harness;

/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

    public static void main(String[] args) throws ClassNotFoundException {

        //TODO: figure out how to get generic class then we don't have to do anything
        /*
        Possibly this in configuration file
        {
           "class":"java.util.ArrayList<Foo>"
           ...Definition for Foo class...
        }
        Size is determined by a bytereader of a single integer, long etc.
         */
        System.out.println((int) '\7');
    }

    private static class TestGeneric<Generic> {
        Generic generic;
    }
}
