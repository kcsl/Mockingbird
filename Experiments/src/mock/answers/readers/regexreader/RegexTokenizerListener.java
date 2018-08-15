package mock.answers.readers.regexreader;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Derrick Lockwood
 * @created 8/5/18.
 */
public class RegexTokenizerListener extends DefaultRegexListener {

    private final DataInput dataInput;
    private final StringBuilder stringBuilder;
    private int repeats;

    private static final Set<Character> ALL_CHARACTERS;

    static {
        ALL_CHARACTERS = new HashSet<>();
        for (int i = 0; i < 255; i++) {
            ALL_CHARACTERS.add((char) i);
        }
    }

    public RegexTokenizerListener(DataInput dataInput) {
        this.dataInput = dataInput;
        stringBuilder = new StringBuilder();
        repeats = 1;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    @Override
    public void exitText(RegexParser.TextContext ctx) {
        stringBuilder.append(ctx.getText());
    }

    @Override
    public void enterCharacterClassLetters(RegexParser.CharacterClassLettersContext ctx) {
        System.out.println(ctx);
    }

    @Override
    public void exitCharacterClassLetters(RegexParser.CharacterClassLettersContext ctx) {
        try {
            if (ctx.getChildCount() == 1) {
                char[] chars = ctx.getText().toCharArray();
                for (int i = 0; i < repeats; i++) {
                    stringBuilder.append(chars[readByteRange(0, chars.length - 1)]);
                }
            } else {
                char[] chars = ctx.getChild(0).getText().toCharArray();
                Set<Character> characters = IntStream.range(0, chars.length).mapToObj(i -> chars[i]).collect(
                        Collectors.toSet());
                Set<Character> set = new HashSet<>(ALL_CHARACTERS);
                set.removeAll(characters);
                Character[] nonChars = set.toArray(new Character[0]);
                for (int i = 0; i < repeats; i++) {
                    stringBuilder.append(nonChars[readByteRange(0, nonChars.length - 1)]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exitDigit(RegexParser.DigitContext ctx) {
        try {
            for (int i = 0; i < repeats; i++) {
                stringBuilder.append(readByteRange(0, 9));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exitAtLeastOne(RegexParser.AtLeastOneContext ctx) {
        try {
            int tmp = dataInput.readInt();
            repeats = tmp < 1 ? 1 : tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exitAnyAmount(RegexParser.AnyAmountContext ctx) {
        try {
            int tmp = dataInput.readInt();
            repeats = tmp < 0 ? 0 : tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exitRange(RegexParser.RangeContext ctx) {
        int lowerBound = Integer.parseInt(ctx.getChild(1).getText());
        int upperBound = Integer.parseInt(ctx.getChild(3).getText());
        try {
            repeats = readIntRange(lowerBound, upperBound);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
        throw new RuntimeException("Can't parse " + errorNode);
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
        repeats = 1;
    }

    private int readIntRange(double min, double max) throws IOException {
        double range = max - min;
        return (int) (((((double) dataInput.readInt() - (double) Integer.MIN_VALUE) * range) / ((double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE)) + min);
    }

    private int readByteRange(double min, double max) throws IOException {
        double range = max - min;
        return (int) (((((double) dataInput.readByte() - (double) Byte.MIN_VALUE) * range) / ((double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE)) + min);
    }
}
