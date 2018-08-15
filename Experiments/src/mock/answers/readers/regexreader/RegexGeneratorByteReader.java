package mock.answers.readers.regexreader;

import mock.answers.readers.ByteReader;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Derrick Lockwood
 * @created 8/2/18.
 */
public class RegexGeneratorByteReader extends ByteReader {

    private static final Set<Character> ALL_CHARACTERS;

    static {
        ALL_CHARACTERS = new HashSet<>();
        for (int i = 0; i < 255; i++) {
            ALL_CHARACTERS.add((char) i);
        }
    }

    private final String regex;

    public RegexGeneratorByteReader(String name, String regex) {
        super(name, null, -1);
        this.regex = regex;
    }

    @Override
    protected ByteReader duplicateByteReader() {
        return new RegexGeneratorByteReader(name, regex);
    }

    @Override
    protected void handleReadException(IOException e) {
        throw new RuntimeException("Couldn't finish reading regex " + regex, e);
    }

    /**
     * Grabs the number of repeat of characters '+' and '*' or nothing
     * <p>
     * NOTE: maybe more than 2 billion repeating? unsigned integer?
     *
     * @param dataInput
     * @return
     */
    private int parseRepeat(String s, MutableInteger index, DataInput dataInput) throws IOException {
        if (index.value >= s.length()) {
            return 1;
        }
        switch (s.charAt(index.value)) {
            case '+':
                index.increment();
                int r = dataInput.readInt();
                return (r <= 0 ? 1 : r);
            case '*':
                index.increment();
                r = dataInput.readInt();
                return (r < 0 ? 0 : r);
            case '{':
                index.increment();
                int end = s.indexOf('}', index.value);
                String tmp = s.substring(index.value, end - 1);
                String[] args = tmp.split(",", 1);
                int minRepeat = Integer.parseInt(args[0]);
                int maxRepeat = Integer.parseInt(args[1]);
                if (minRepeat > maxRepeat) {
                    int tmpRepeat = minRepeat;
                    minRepeat = maxRepeat;
                    maxRepeat = tmpRepeat;
                }
                int number = (int) (((((double) dataInput.readInt() - (double) Integer.MIN_VALUE) * (maxRepeat - minRepeat)) / ((double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE)) + minRepeat);
                index.increment(end - index.value);
                return number;
            default:
                return 1;
        }
    }

    private int readDigit(DataInput dataInput) throws IOException {
        byte b = dataInput.readByte();
        return (int) (((((double) b - (double) Byte.MIN_VALUE) * 9) / ((double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE)) + 0);
    }

    private char readNonDigit(DataInput dataInput) throws IOException {
        int b = dataInput.readUnsignedByte();
        if (b >= 48 && b <= 57) {
            b += 10;
        }
        return (char) b;
    }

    private void parseCharacterClass(String s, MutableInteger index, StringBuilder stringBuilder,
            DataInput dataInput) throws
            IOException {
        int end = s.indexOf(']', index.value);
        String inString = s.substring(index.value, end);
        index.increment(end - index.value + 1);
        int repeat = parseRepeat(s, index, dataInput);
        boolean notCharacters = inString.startsWith("^");
        if (notCharacters) {
            inString = inString.substring(1);
        }
        if (inString.indexOf('-') > 0) {
            String[] strings = inString.split("-");
            char startChar = strings[0].charAt(0);
            char endChar = strings[1].charAt(0);
            if (!notCharacters) {
                for (int i = 0; i < repeat; i++) {
                    stringBuilder.append((char) readByteRange(dataInput, startChar, endChar));
                }
            } else {
                Set<Character> set = new HashSet<>(ALL_CHARACTERS);
                List<Character> characterList = set.stream().filter(
                        character -> character > endChar || character < startChar).collect(Collectors.toList());
                for (int i = 0; i < repeat; i++) {
                    stringBuilder.append(characterList.get(readByteRange(dataInput, 0, characterList.size())));
                }
            }

        } else {
            if (!notCharacters) {
                char[] chars = inString.toCharArray();
                for (int i = 0; i < repeat; i++) {
                    stringBuilder.append(chars[readByteRange(dataInput, 0, chars.length - 1)]);
                }
            } else {
                char[] chars = inString.toCharArray();
                Set<Character> characters = IntStream.range(0, chars.length).mapToObj(i -> chars[i]).collect(
                        Collectors.toSet());
                Set<Character> set = new HashSet<>(ALL_CHARACTERS);
                set.removeAll(characters);
                Character[] nonChars = set.toArray(new Character[0]);
                for (int i = 0; i < repeat; i++) {
                    stringBuilder.append(nonChars[readByteRange(dataInput, 0, nonChars.length - 1)]);
                }
            }
        }
    }

    private int readByteRange(DataInput dataInput, double min, double max) throws IOException {
        double range = max - min;
        return (int) (((((double) dataInput.readByte() - (double) Byte.MIN_VALUE) * range) / ((double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE)) + min);
    }

    private void parseEscapeCharacter(String s, MutableInteger index, StringBuilder stringBuilder,
            DataInput dataInput) throws
            IOException {
        char c = s.charAt(index.value);
        index.increment();
        int repeats = parseRepeat(regex, index, dataInput);
        switch (c) {
            case 'd':
                for (int i = 0; i < repeats; i++) {
                    stringBuilder.append(readDigit(dataInput));
                }
                break;
            case 'D':
                for (int i = 0; i < repeats; i++) {
                    stringBuilder.append(readNonDigit(dataInput));
                }
        }
    }

    private String readFormattedString(DataInput dataInput) throws IOException {
        StringBuilder formattedString = new StringBuilder();
        MutableInteger mutableInteger = new MutableInteger(0);
        for (; mutableInteger.value < regex.length(); mutableInteger.increment()) {
            switch (regex.charAt(mutableInteger.value)) {
                case '\\':
                    mutableInteger.increment();
                    parseEscapeCharacter(regex, mutableInteger, formattedString, dataInput);
                    break;
                case '[':
                    mutableInteger.increment();
                    parseCharacterClass(regex, mutableInteger, formattedString, dataInput);
                    break;
                default:
                    formattedString.append(regex.charAt(mutableInteger.value));
                    break;
            }
        }

        return formattedString.toString();
    }

    @Override
    protected Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) throws IOException {
        if (returnType.isAssignableFrom(String.class)) {
            return readFormattedString(dataInput);
        }
        return null;
    }

    @Override
    protected Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }

    private class MutableInteger extends Number implements Comparable<MutableInteger> {
        int value;

        public MutableInteger(int value) {
            this.value = value;
        }

        public void increment() {
            increment(1);
        }

        public void increment(int increment) {
            value += increment;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public int compareTo(MutableInteger o) {
            return Integer.compare(value, o.value);
        }
    }
}
