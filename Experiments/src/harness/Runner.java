package harness;

import harness.plaittesting.Plait;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

    private static Map<Integer, Integer> map;
    private static boolean isRunning;

    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

        //TODO: figure out how to get generic class then we don't have to do anything
        /*
        Possibly this in configuration file
        {
           "class":"java.util.ArrayList<Foo>"
           ...Definition for Foo class...
        }

        Size is determined by a bytereader of a single integer, long etc.
         */
        Plait plait = new Plait("\u00e0", 8);
        plait.normalizeCompletely();
//        map = new HashMap<>();
//        map.put(null, 100);
//        System.out.println(map.containsKey(null));
//        Random r;
//        map.put(-1, -1);
//        isRunning = true;
//        Thread thread = new Thread(() -> {
//            while (isRunning) {
//                System.out.println(map.get(-1));
//            }
//        });
//        thread.start();
//        Thread.sleep(500);
//        System.out.println("Start");
//        for (int i = 0; i < 10000; i++) {
//            map.put(i, i);
//        }
//        isRunning = false;
//        Thread.sleep(500);
//        System.out.println("End: " + thread.isAlive() + " | " + isRunning);
    }

    public static String test() {
        try {
            System.out.println("In");
            try {
                System.out.println("In2");
            } finally {
                System.out.println("Here");
                return "Out2";
            }
        } finally {
            return "Out";
        }

    }
}
