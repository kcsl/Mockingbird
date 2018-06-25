package method.callbacks;

import method.MethodData;

import java.io.IOException;
import java.util.logging.*;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class LogMethodCallback implements MethodCallback {

    private static final Level VERBOSE_LEVEL = Level.INFO;
    private final Logger logger;
    private final boolean verbose;

    private LogMethodCallback(Logger logger, boolean verbose, Handler... handlers) {
        this.logger = logger;
        for (Handler handler : handlers) {
            this.logger.addHandler(handler);
        }
        this.verbose = verbose;

    }

    public static MethodCallback create(Logger logger, boolean verbose, Handler... handlers) {
        return new LogMethodCallback(logger, verbose, handlers);
    }

    public static MethodCallback create(Logger logger, boolean verbose) {
        return new LogMethodCallback(logger, verbose);
    }

    public static MethodCallback create(
            String subSystemName,
            Level level,
            boolean useParentHandlers,
            boolean verbose,
            Handler... handlers) {
        Logger logger = Logger.getLogger(subSystemName);
        logger.setUseParentHandlers(useParentHandlers);
        logger.setLevel(level);
        return new LogMethodCallback(logger, verbose, handlers);
    }

    public static MethodCallback createConsoleOut(Logger logger, Formatter formatter) {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        return new LogMethodCallback(logger, false, consoleHandler);
    }

    public static MethodCallback createFileOut(Logger logger, String filePath, Formatter formatter) throws IOException {
        FileHandler fileHandler = new FileHandler(filePath);
        fileHandler.setFormatter(formatter);
        return new LogMethodCallback(logger, false, fileHandler);
    }

    private void entering(String sourceClass, String sourceMethod, Object... params) {
        StringBuilder msg = new StringBuilder("ENTER");
        if (params == null) {
            logger.logp(VERBOSE_LEVEL, sourceClass, sourceMethod, msg.toString());
            return;
        }
        if (!logger.isLoggable(VERBOSE_LEVEL))
            return;
        for (int i = 0; i < params.length; i++) {
            msg.append(" {").append(i).append("}");
        }
        logger.logp(VERBOSE_LEVEL, sourceClass, sourceMethod, msg.toString(), params);
    }

    private void exiting(String sourceClass, String sourceMethod, Object result) {
        logger.logp(VERBOSE_LEVEL, sourceClass, sourceMethod, "RETURN {0}", result);
    }

    private void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        if (!logger.isLoggable(VERBOSE_LEVEL)) {
            return;
        }
        LogRecord lr = new LogRecord(VERBOSE_LEVEL, "THROW");
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        lr.setLoggerName(logger.getName());
        lr.setResourceBundle(logger.getResourceBundle());
        lr.setResourceBundleName(logger.getResourceBundleName());
        logger.log(lr);
    }

    @Override
    public void onBefore(MethodData methodData) {
        if (verbose) {
            entering(methodData.getDeclaringClass().getName(), methodData.getMethodName(), methodData.getParameters());
        }
    }

    @Override
    public void onAfter(MethodData methodData) {
        logger.logp(Level.INFO, methodData.getDeclaringClass().getName(), methodData.getMethodName(),
                methodData.toString());
        if (methodData.getReturnException() != null) {
            throwing(methodData.getDeclaringClass().getName(), methodData.getMethodName(),
                    methodData.getReturnException().getCause());
        }
        if (verbose) {
            exiting(methodData.getDeclaringClass().getName(), methodData.getMethodName(), methodData.getReturnValue());
        }
    }

    @Override
    public void onEndIteration() {

    }

    @Override
    public boolean continueIteration() {
        return false;
    }

}
