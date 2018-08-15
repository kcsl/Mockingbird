package harness;

import testagents.ReplicatedRandom;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

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

//        Random random = new Random();
//        int mod = 5;
//        int[] r = new int[3];
//        for (int i = 0 ; i<r.length; i++){
//            r[i] = random.nextInt(mod);
//        }
//        System.out.println(Arrays.toString(r));
//        double a = ((r[1] - r[2])/(r[0] - r[1])) % mod;
//        double c = ((r[0]*r[2] - r[1])/(r[0] - r[1])) % mod;
//        int next = random.nextInt(mod);
//        int pred = (int) ((a*next + c) % mod);
//        System.out.println(pred + " : " + random.nextInt(mod));
        Random random = new Random();
        ReplicatedRandom replicatedRandom = new ReplicatedRandom();
        int mod = 20;
        int prev = random.nextInt(mod);
        int cur = random.nextInt(mod);
        boolean found = false;
        while (!found) {
            if (!replicatedRandom.replicateState(prev, cur)){
                prev = cur;
                cur = random.nextInt(mod);
                continue;
            }
            int r1 = random.nextInt(mod);
            int r2 = replicatedRandom.nextInt(mod);
            System.out.println(r1 + " | " + r2);
            if (r1 != r2) {
                prev = cur;
                cur = random.nextInt(mod);
            } else {
                found = true;
            }
        }
        System.out.println(random.nextInt(mod) + " : " + replicatedRandom.nextInt(mod));
    }

    private static class TestGeneric<Generic> {
        Generic generic;
    }
}
