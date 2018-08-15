package mock.answers.readers.regexreader;

import mock.answers.readers.ByteReader;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Derrick Lockwood
 * @created 8/5/18.
 */
public class RegexTokenizerByteReader extends ByteReader {

    private final RegexParser parser;
    private final RegexTokenizerVisitor visitor;
    private final String regex;

    private static RegexParser getParserFromString(String regex) throws IOException {
        RegexLexer lexer = new RegexLexer(CharStreams.fromStream(new ByteArrayInputStream(regex.getBytes())));
        return new RegexParser(new CommonTokenStream(lexer));
    }

    public RegexTokenizerByteReader(String name, String regex) throws IOException {
        this(name, regex, getParserFromString(regex));
    }

    private RegexTokenizerByteReader(String name, String regex, RegexParser parser) {
        super(name, null, -1);
        this.parser = parser;
        this.regex = regex;
        visitor = new RegexTokenizerVisitor();
    }

    @Override
    protected ByteReader duplicateByteReader() {
        return new RegexTokenizerByteReader(name, regex, parser);
    }

    @Override
    protected void handleReadException(IOException e) {
        throw new RuntimeException("Couldn't finish reading regex " + regex, e);
    }

    private String readRegexString(DataInput dataInput) {
        visitor.setDataInput(dataInput);
        return visitor.visit(parser.main());
    }

    @Override
    protected Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) throws IOException {
        if (returnType.isAssignableFrom(String.class)) {
            return readRegexString(dataInput);
        }
        return null;
    }

    @Override
    protected Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }
}
