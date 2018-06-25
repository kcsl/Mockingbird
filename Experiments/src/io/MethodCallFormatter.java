package io;

import mock.answers.NotStubbedException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Derrick Lockwood
 * @created 6/19/18.
 */
public class MethodCallFormatter extends Formatter {
    private final Date date = new Date();
    private final String format;

    public MethodCallFormatter(String format) {
        this.format = format;
    }

    @Override
    public String format(LogRecord record) {
        date.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return String.format(format,
                date,
                source,
                record.getLoggerName(),
                record.getLevel().getName(),
                message,
                throwable);
    }

    @Override
    public synchronized String formatMessage(LogRecord record) {
        String format = record.getMessage();
        java.util.ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            try {
                format = catalog.getString(record.getMessage());
            } catch (java.util.MissingResourceException ex) {
                // Drop through.  Use record message as format
                format = record.getMessage();
            }
        }
        // Do the formatting.
        try {
            Object parameters[] = record.getParameters();
            if (parameters == null || parameters.length == 0) {
                // No parameters.  Just return format string.
                return format;
            }
            if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 ||
                    format.indexOf("{2") >= 0 || format.indexOf("{3") >= 0) {
                for (int i = 0; i < parameters.length; i++) {
                    format = format.replace("{" + i + "}", getFormat(parameters[i]));
                }
                return format;
            }
            return format;

        } catch (Exception ex) {
            ex.printStackTrace();
            // Formatting failed: use localized format string.
            return format;
        }
    }

    private synchronized String getFormat(Object parameter) {
        try {
            return Objects.toString(parameter);
        } catch (NotStubbedException e) {
            return "NO-TOSTRING:" + parameter.getClass().getName();
        }
    }
}
