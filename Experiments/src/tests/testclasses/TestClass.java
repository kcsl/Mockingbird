package testclasses;

import java.util.ArrayList;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TestClass {
    public void testArray(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 100 && arr[i] < 500) {
                throw new RuntimeException("I HATE ARRAYS and LISTS and HASHMAPS!!!!!!!!");
            }
        }
    }

    public void testArrayObj(Foo[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getA() > 100 && arr[i].getA() < 500) {
                throw new RuntimeException("I HATE ARRAYS and LISTS and HASHMAPS!!!!!!!!");
            }
        }
    }

    public void testList(ArrayList<Integer> arr) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) > 100 && arr.get(i) < 500) {
                throw new RuntimeException("I HATE ARRAYS and LISTS and HASHMAPS!!!!!!!!");
            }
        }
    }

    public void testListObj(ArrayList<Foo> arr) {
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).getA() > 100 && arr.get(i).getA() < 500) {
                throw new RuntimeException("I HATE ARRAYS and LISTS and HASHMAPS!!!!!!!!");
            }
        }
    }
}
