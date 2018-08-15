package mock.answers.readers;

import com.mifmif.common.regex.Generex;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author Derrick Lockwood
 * @created 8/6/18.
 */
public class NaiveRegexGeneratorByteReader extends ByteReader {

    private final String regex;
    private final Generex generex;

    public NaiveRegexGeneratorByteReader(String name, String regex) {
        super(name, null, -1);
        this.regex = regex;
        generex = new Generex(regex);
    }

    private NaiveRegexGeneratorByteReader(String name, Generex generex, String regex) {
        super(name, null, -1);
        this.generex = generex;
        this.regex = regex;
    }

    @Override
    protected ByteReader duplicateByteReader() {
        return new NaiveRegexGeneratorByteReader(name, generex, regex);
    }

    @Override
    protected void handleReadException(IOException e) {
        throw new RuntimeException(e);
    }

    @Override
    protected Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) throws IOException {
        if (returnType.isAssignableFrom(String.class)) {
            return generex.getMatchedString(dataInput.readInt());
        }
        return null;
    }

    @Override
    protected Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }
}
