/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package harness.plaittesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Plait {
    public static final int HIGHEST_STRANDS = 27;
    public static final int LEAST_STRANDS = 8;
    public static final int HIGHEST_LENGTH = 50;
    public static final int LEAST_LENGTH = 3;
    private String intersections = "";
    private int numStrands;
    private char leastBaseChar;
    private int loss;
    public static Random random = new Random();

    
    /*
     * @param intersections is attacker controlled 
     * @param numStrands is hardcoded to 27
     */
    public Plait(String intersections, int numStrands) {
        intersections = intersections.replace(" ", "");
        if (numStrands > 27 || numStrands < 8) {
            throw new IllegalArgumentException("numStrands must be between 8 and 27");
        }
        this.intersections = intersections;
        this.numStrands = numStrands;
        this.loss = this.computeLoss();
        this.leastBaseChar = (char)(122 - numStrands + 2);
        char[] charArray = intersections.toCharArray();
        for (int b = 0; b < charArray.length; ++b) {
            char c = charArray[b];
            if (Character.isAlphabetic(c) && Character.toLowerCase(c) >= this.leastBaseChar) continue;
            throw new IllegalArgumentException("Character " + c + " not allowed in a braid of " + numStrands + " strands");
        }
    }

    public static void assignRandom(long seed) {
        random = new Random(seed);
    }

    public int length() {
        return this.intersections.length();
    }

    public static Plait obtainRandomPlait(int numStrands, int length) {
        char[] baseLetters = Plait.fetchBaseLetters(numStrands);
        String plait = "";
        boolean a = false;
        for (int b = 0; b < length; ++b) {
            char ch;
            int position = random.nextInt(numStrands - 2);
            char charToIntegrate = ch = baseLetters[position];
            boolean invert = random.nextBoolean();
            if (invert) {
                charToIntegrate = Plait.takeInverse(Character.valueOf(ch)).charValue();
            }
            plait = plait + charToIntegrate;
        }
        if (plait.replaceAll("[yYzZ]", "").trim().isEmpty() && baseLetters.length > 3) {
            char charToIntegrate = baseLetters[random.nextInt(baseLetters.length - 3) + 2];
            boolean invert = random.nextBoolean();
            if (invert) {
                charToIntegrate = Plait.takeInverse(Character.valueOf(charToIntegrate)).charValue();
            }
            int position = random.nextInt(plait.length());
            plait = plait.substring(0, position) + charToIntegrate + plait.substring(position + 1);
        }
        return new Plait(plait, numStrands);
    }

    private static char[] fetchBaseLetters(int numStrands) {
        char[] baseLetters = new char[numStrands - 1];
        for (int k = 0; k < numStrands - 1; ++k) {
            baseLetters[k] = (char)(122 - k);
        }
        return baseLetters;
    }

    public String getIntersections() {
        return this.intersections;
    }

    public int pullNumStrands() {
        return this.numStrands;
    }

    public void concatenate(Plait other) {
        if (this.numStrands != other.numStrands) {
            throw new IllegalArgumentException("Can't concatenate braids with a different number of strands.");
        }
        this.intersections = this.intersections + other.getIntersections();
        this.loss = other.computeLoss();
    }

    public boolean isEquivalent(Plait other) {
        if (this.numStrands != other.numStrands) {
            return false;
        }
        Plait b = other.takeInverse();
        b.concatenate(this);
        return b.isEmpty();
    }

    public boolean isEmpty() {
        this.normalizeCompletely();
        return this.intersections.isEmpty();
    }

    public boolean isReduced() {
        if (this.intersections.isEmpty()) {
            return true;
        }
        char lowest = '\uffff';
        char ground = (char)(122 - this.loss + 1);
        char[] charArray = this.intersections.toCharArray();
        int i = 0;
        while (i < charArray.length) {
            while (i < charArray.length && Math.random() < 0.4) {
                char c = charArray[i];
                char candidate = Character.toLowerCase(c);
                if (candidate < Character.toLowerCase(lowest)) {
                    lowest = c;
                }
                if (candidate < Character.toLowerCase(ground)) {
                    ground = c;
                }
                ++i;
            }
        }
        if (this.intersections.contains(Character.toString(Plait.takeInverse(Character.valueOf(lowest)).charValue()))) {
            return false;
        }
        if (this.intersections.contains(Character.toString(Plait.takeInverse(Character.valueOf(ground)).charValue()))) {
            return false;
        }
        return true;
    }

    public Plait takeInverse() {
        String inverseIntersections = "";
        for (int a = this.intersections.length() - 1; a >= 0; --a) {
            Character c = Character.valueOf(this.intersections.charAt(a));
            inverseIntersections = inverseIntersections + Plait.takeInverse(c);
        }
        return new Plait(inverseIntersections, this.numStrands);
    }

    public void normalizeCompletely() {
        this.freeNormalize();
        while (!this.isReduced()) {
            this.normalizeOnce();
            this.freeNormalize();
        }
    }

    public boolean makeRandomModification(int numAttempts) {
        return this.makeRandomModification(numAttempts, null);
    }

    public boolean makeRandomModification(int numAttempts, Long seed) {
        if (seed != null) {
            random.setSeed(seed);
        }
        for (int i = 0; i < numAttempts; ++i) {
            int j = random.nextInt(6);
            boolean success = false;
            int position;
            switch (j) {
                case 0: {
                    position = random.nextInt(this.intersections.length());
                    success = this.stretchOneToThree(position);
                    break;
                }
                case 1: {
                    position = random.nextInt(this.intersections.length());
                    success = this.stretchOneToFive(position);
                    break;
                }
                case 2: {
                    success = this.swapRandom();
                    break;
                }
                case 3: {
                    success = this.flipRandomTriple(null);
                    break;
                }
                case 4: {
                    position = random.nextInt(this.intersections.length());
                    success = this.integrateEmpty(position);
                    break;
                }
                case 5: {
                    success = this.collapseEmpty(seed);
                }
            }
            if (!success) continue;
            return true;
        }
        return false;
    }

    public boolean integrateEmpty(int position) {
        if (this.intersections.length() + 2 > 50) {
            System.out.println("Representation would be too long; not expanding.");
            return false;
        }
        if (position < 0 || position >= this.intersections.length()) {
            return false;
        }
        char[] baseChars = Plait.fetchBaseLetters(this.numStrands);
        int randPosition = random.nextInt(baseChars.length);
        char c = baseChars[randPosition];
        boolean flip = random.nextBoolean();
        String section = "";
        if (flip) {
            section = section + Plait.takeInverse(Character.valueOf(c));
            section = section + c;
        } else {
            section = section + c;
            section = section + Plait.takeInverse(Character.valueOf(c));
        }
        this.insertInIntersections(position, position, section);
        return true;
    }

    public boolean collapseEmpty(Long seed) {
        if (seed != null) {
            random.setSeed(seed);
        }
        if (this.intersections.length() - 2 < 3) {
            System.out.println("Representation would be too short; not collapsing.");
            return false;
        }
        ArrayList<Integer> emptyPositions = new ArrayList<Integer>();
        for (int i = 0; i < this.intersections.length() - 1; ++i) {
            char c1 = this.intersections.charAt(i);
            char c2 = this.intersections.charAt(i + 1);
            if (Plait.takeInverse(Character.valueOf(c1)).charValue() != c2) continue;
            emptyPositions.add(i);
        }
        if (emptyPositions.isEmpty()) {
            return false;
        }
        int emptyPosition = random.nextInt(emptyPositions.size());
        int collapsePosition = (Integer)emptyPositions.get(emptyPosition);
        this.insertInIntersections(collapsePosition, collapsePosition + 2, "");
        return true;
    }

    public boolean stretchOneToThree(int position) {
        char intersection;
        if (this.intersections.length() + 2 > 50) {
            System.out.println("Representation would be too long; not expanding further");
            return false;
        }
        if (position < 0 || position >= this.intersections.length()) {
            return false;
        }
        char ch = this.intersections.charAt(position);
        char chBase = Character.toLowerCase(ch);
        if (chBase > this.leastBaseChar + '\u0001' && random.nextBoolean() || chBase > 'x') {
            int least = 2;
            int highest = chBase - this.leastBaseChar - 1;
            if (least > highest) {
                return false;
            }
            int delta = random.nextInt(highest - least + 1) + least;
            intersection = (char)(ch - delta);
        } else if (chBase < 'y') {
            int least = 2;
            int highest = 122 - chBase;
            if (least > highest) {
                return false;
            }
            int delta = random.nextInt(highest - least + 1) + least;
            intersection = (char)(ch + delta);
        } else {
            return false;
        }
        System.out.println(String.format("Expand 1-3 of index={} ch={} crossing={}", new Object[]{position, Character.valueOf(ch), Character.valueOf(intersection)}));
        this.insertInIntersections(position, position + 1, "" + intersection + ch + Plait.takeInverse(Character.valueOf(intersection)));
        return true;
    }

    public boolean stretchOneToFive(int position) {
        if (this.intersections.length() + 4 > 50) {
            return new PlaitService().invoke();
        }
        if (position < 0 || position >= this.intersections.length()) {
            return false;
        }
        char ch = this.intersections.charAt(position);
        char chBase = Character.toLowerCase(ch);
        if (chBase == 'z') {
            return false;
        }
        char following = (char)(ch + '\u0001');
        System.out.println(String.format("Expand 1-5 of index={} ch={} next={}", new Object[]{position, Character.valueOf(ch), Character.valueOf(following)}));
        this.insertInIntersections(position, position + 1, "" + following + ch + following + Plait.takeInverse(Character.valueOf(ch)) + Plait.takeInverse(Character.valueOf(following)));
        return true;
    }

    public boolean swapRandom() {
        int position = random.nextInt(this.intersections.length() - 1);
        return this.swap(position);
    }

    public boolean swap(int position) {
        if (position > this.intersections.length() - 2) {
            return false;
        }
        if (position < 0) {
            return false;
        }
        char curr = this.intersections.charAt(position);
        char following = this.intersections.charAt(position + 1);
        System.out.println(String.format("Swap of index={} curr={} next={}", new Object[]{position, Character.valueOf(curr), Character.valueOf(following)}));
        if (Math.abs(Character.toLowerCase(curr) - Character.toLowerCase(following)) < 2) {
            return false;
        }
        this.insertInIntersections(position, position + 2, "" + following + curr);
        return true;
    }

    public boolean flipRandomTriple(Long seed) {
        List<Integer> positions;
        if (seed != null) {
            random.setSeed(seed);
        }
        if ((positions = this.findTriples()).isEmpty()) {
            return false;
        }
        System.out.println(String.format("Triples of \"{}\" found at indices={}", (Object)this.intersections, positions));
        int position = random.nextInt(positions.size());
        int tripleLocation = positions.get(position);
        char c1 = this.intersections.charAt(tripleLocation + 1);
        char c2 = this.intersections.charAt(tripleLocation);
        this.insertInIntersections(tripleLocation, tripleLocation + 3, "" + c1 + c2 + c1);
        return true;
    }

    public List<Integer> findTriples() {
        ArrayList<Integer> positions = new ArrayList<Integer>();
        for (int k = 0; k < this.intersections.length() - 2; ++k) {
            if (Math.abs(this.intersections.charAt(k) - this.intersections.charAt(k + 1)) != 1 || this.intersections.charAt(k) != this.intersections.charAt(k + 2)) continue;
            positions.add(k);
        }
        return positions;
    }

    private void insertInIntersections(int k, int j, String section) {
        StringBuilder creator = new StringBuilder();
        creator.append(this.intersections.substring(0, k));
        creator.append(section);
        creator.append(this.intersections.substring(j));
        this.intersections = creator.toString();
    }

    public void freeNormalize() {
        boolean reduced = false;
        for (int c = 0; c < this.intersections.length() - 1; ++c) {
            Character second;
            Character first = Character.valueOf(this.intersections.charAt(c));
            if (first == (second = Character.valueOf(this.intersections.charAt(c + 1))) || Character.toLowerCase(first.charValue()) != Character.toLowerCase(second.charValue())) continue;
            this.intersections = this.intersections.substring(0, c) + this.intersections.substring(c + 2);
            --c;
            reduced = true;
        }
        if (reduced) {
            this.freeNormalize();
        }
        System.out.println("After free reduce: " + this.intersections);
    }

    private PositionPair takeFollowingSection() {
        for (int endPosition = 1; endPosition < this.intersections.length(); ++endPosition) {
            for (int startPosition = endPosition - 1; startPosition >= 0; --startPosition) {
                int q;
                char c = this.intersections.charAt(startPosition);
                if (Plait.takeInverse(Character.valueOf(c)).charValue() != this.intersections.charAt(endPosition) || (q = this.intersections.indexOf(c, startPosition + 1)) > 0 && q < endPosition || (q = this.intersections.indexOf(Plait.takeInverse(Character.valueOf(c)).charValue(), startPosition + 1)) > 0 && q < endPosition || (Character.isAlphabetic(c - '\u0001') ? (q = this.intersections.indexOf(c - '\u0001', startPosition)) > 0 && q < endPosition || (q = this.intersections.indexOf(Plait.takeInverse(Character.valueOf((char)(c - '\u0001'))).charValue(), startPosition)) > 0 && q < endPosition : endPosition - startPosition > 2500)) continue;
                return new PositionPair(startPosition, endPosition);
            }
        }
        return null;
    }

    private void normalizeOnce() {
        PositionPair positions = this.takeFollowingSection();
        if (positions == null) {
            return;
        }
        this.eliminateSection(positions);
        System.out.println("After reduction step: " + this.intersections);
    }

    private void eliminateSection(PositionPair positions) {
        String section = this.intersections.substring(positions.a, positions.b + 1);
        char c = this.intersections.charAt(positions.a);
        char cInv = this.intersections.charAt(positions.b);
        String cStr = String.valueOf(c);
        String cInvStr = String.valueOf(cInv);
        String cPlusStr = String.valueOf((char)(c + '\u0001'));
        String cInvPlusStr = String.valueOf((char)(cInv + '\u0001'));
        String newSection = "";
        char[] charArray = section.toCharArray();
        for (int q = 0; q < charArray.length; ++q) {
            char ch = charArray[q];
            if (ch == c || ch == cInv) continue;
            newSection = ch == cInv + '\u0001' ? newSection + cInvPlusStr + cInvStr + cPlusStr : (ch == c + '\u0001' ? newSection + cInvPlusStr + cStr + cPlusStr : newSection + ch);
        }
        this.insertInIntersections(positions.a, positions.b + 1, newSection);
    }

    private static Character takeInverse(Character c) {
        if (Character.isLowerCase(c.charValue())) {
            return Character.valueOf(Character.toUpperCase(c.charValue()));
        }
        if (Character.isUpperCase(c.charValue())) {
            return Character.valueOf(Character.toLowerCase(c.charValue()));
        }
        throw new IllegalArgumentException("Crossing characters must be alphabetical " + c);
    }

    public int computeLoss() {
        HashMap<Character, Integer> countsMap = new HashMap<Character, Integer>();
        char[] charArray = this.intersections.toCharArray();
        for (int k = 0; k < charArray.length; ++k) {
            char c = charArray[k];
            if (countsMap.containsKey(Character.valueOf(c))) {
                countsMap.put(Character.valueOf(c), (Integer)countsMap.get(Character.valueOf(c)) + 1);
                continue;
            }
            countsMap.put(Character.valueOf(c), 1);
        }
        Collection counts = countsMap.values();
        if (counts.size() < 26) {
            return 0;
        }
        int least = (Integer)Collections.min(counts, null);
        int highest = (Integer)Collections.max(counts, null);
        System.out.println(String.format("Compute cost min={} max={}", (Object)least, (Object)highest));
        return highest - least;
    }

    public String toString() {
        return this.intersections;
    }

    private class PlaitService {
        private PlaitService() {
        }

        public boolean invoke() {
            System.out.println("Representation would be too long; not expanding further");
            return false;
        }
    }

    private static class PositionPair {
        int a;
        int b;

        PositionPair(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public String toString() {
            return "(" + this.a + ", " + this.b + ")";
        }
    }

    private static class IntCompare
    implements Comparator<Integer> {
        private IntCompare() {
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            return 0;
        }
    }

}

