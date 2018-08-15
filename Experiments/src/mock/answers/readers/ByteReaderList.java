package mock.answers.readers;

import method.MethodData;
import method.callbacks.MethodCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * @author Derrick Lockwood
 * @created 6/20/18.
 */
public class ByteReaderList implements MethodCallback {

    private final List<ByteReader> byteReaders;
    private final Logger logger;

    public ByteReaderList(Logger logger) {
        byteReaders = new ArrayList<>();
        this.logger = logger;
    }

    public static Formatter getFormatter() {
        return new Formatter() {
            @Override
            public String format(LogRecord record) {
                return formatMessage(record) + "\n";
            }
        };
    }

    public static Filter getFilter() {
        return record -> record.getMessage().contains(
                "BYTEREADERS") || record.getLevel().intValue() >= Level.WARNING.intValue();
    }

    public void add(ByteReader byteReader) {
        byteReader.setByteReaderListLink(this);
        this.byteReaders.add(byteReader);
    }

    public boolean remove(ByteReader byteReader) {
        return byteReaders.remove(byteReader);
    }

    public void setInputStream(InputStream inputStream) throws IOException {
        for (ByteReader byteReader : byteReaders) {
            byteReader.setInputStream(inputStream);
        }
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {
        if (byteReaders.size() == 0) {
            return;
        }
        StringBuilder msg = new StringBuilder("BYTEREADERS");
        if (!logger.isLoggable(Level.INFO))
            return;
        for (int i = 0; i < byteReaders.size(); i++) {
            msg.append(" |{").append(i).append("}|");
        }
        logger.logp(Level.INFO, methodData.getDeclaringClass().getName(), methodData.getMethodName(), msg.toString(),
                byteReaders.toArray(new ByteReader[0]));
    }

    @Override
    public void onEndIteration() {

    }

    @Override
    public boolean continueIteration() {
        return false;
    }
}
