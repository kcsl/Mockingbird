package mock.answers.readers.regexreader;

import java.io.DataInput;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Derrick Lockwood
 * @created 8/6/18.
 */
public class RegexTokenizerVisitor extends RegexBaseVisitor<String> {

    private DataInput dataInput;
    private int repeats;

    public RegexTokenizerVisitor() {
        repeats = 1;
    }

    public void setDataInput(DataInput dataInput) {
        this.dataInput = dataInput;
    }

    @Override
    public String visitText(RegexParser.TextContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitDigit(RegexParser.DigitContext ctx) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < repeats; i++) {
                stringBuilder.append(readByteRange(0, 9));
            }
            repeats = 1;
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String visitAtLeastOne(RegexParser.AtLeastOneContext ctx) {
        try {
            int tmp = dataInput.readInt();
            repeats = tmp < 1 ? 1 : tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String visitAnyAmount(RegexParser.AnyAmountContext ctx) {
        try {
            int tmp = dataInput.readInt();
            repeats = tmp < 0 ? 0 : tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String visitRange(RegexParser.RangeContext ctx) {
        int lowerBound = Integer.parseInt(ctx.getChild(1).getText());
        int upperBound = Integer.parseInt(ctx.getChild(3).getText());
        try {
            repeats = readIntRange(lowerBound, upperBound);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private String getNullOrEmpty(String s) {
        return s == null ? "" : s;
    }

    private int readIntRange(double min, double max) throws IOException {
        double range = max - min;
        return (int) (((((double) dataInput.readInt() - (double) Integer.MIN_VALUE) * range) / ((double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE)) + min);
    }

    private int readByteRange(double min, double max) throws IOException {
        double range = max - min;
        return (int) (((((double) dataInput.readByte() - (double) Byte.MIN_VALUE) * range) / ((double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE)) + min);
    }

    @Override
    protected String aggregateResult(String aggregate, String nextResult) {
        return getNullOrEmpty(aggregate) + getNullOrEmpty(nextResult);
    }
}
