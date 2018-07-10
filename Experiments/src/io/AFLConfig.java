package io;

import org.json.simple.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Derrick Lockwood
 * @created 6/20/18.
 */
public class AFLConfig {

    private static final String TIMEOUT = "timeout";
    private static final String CONSOLE_LEVEL = "console_log_level";
    private static final String FILE_LEVEL = "file_log_level";
    private static final String LOG_FILE = "log_file";
    private static final String BYTE_READER_OUT_ONLY = "byte_reader_out_only";
    private static final String REFRESH_OBJECTS = "refresh_objects";
    private static final String LOG_TO_CSV = "log_to_csv";

    private static Map<String, Object> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(TIMEOUT, 30000);
        DEFAULTS.put(CONSOLE_LEVEL, Level.INFO);
        DEFAULTS.put(FILE_LEVEL, Level.INFO);
        DEFAULTS.put(LOG_FILE, null);
        DEFAULTS.put(BYTE_READER_OUT_ONLY, false);
        DEFAULTS.put(REFRESH_OBJECTS, false);
        DEFAULTS.put(LOG_TO_CSV, null);
    }

    public final long timeout;
    public final boolean byteReaderOutOnly;
    public final boolean refreshObjects;
    public final Level consoleLevel;
    public final Level fileLevel;
    public final File logFile;
    public final File logToCSV;

    public AFLConfig() {
        timeout = (long) DEFAULTS.get(TIMEOUT);
        consoleLevel = (Level) DEFAULTS.get(CONSOLE_LEVEL);
        fileLevel = (Level) DEFAULTS.get(FILE_LEVEL);
        logFile = (File) DEFAULTS.get(LOG_FILE);
        byteReaderOutOnly = (boolean) DEFAULTS.get(BYTE_READER_OUT_ONLY);
        refreshObjects = (boolean) DEFAULTS.get(REFRESH_OBJECTS);
        logToCSV = (File) DEFAULTS.get(LOG_TO_CSV);
    }

    public AFLConfig(JSONObject config) {
        timeout = containsOrDefault(config, TIMEOUT);
        byteReaderOutOnly = containsOrDefault(config, BYTE_READER_OUT_ONLY);
        consoleLevel = getLevel(config, CONSOLE_LEVEL);
        fileLevel = getLevel(config, FILE_LEVEL);
        logFile = getFile(config, LOG_FILE);
        refreshObjects = containsOrDefault(config, REFRESH_OBJECTS);
        logToCSV = getFile(config, LOG_TO_CSV);
    }

    private static File getFile(JSONObject config, String name) {
        String filePath = containsOrDefault(config, name);
        if (filePath != null) {
            return new File(filePath);
        }
        return null;
    }

    private static Level getLevel(JSONObject config, String name) {
        return Level.parse(containsOrDefault(config, name, ((Level) DEFAULTS.get(name)).getName()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T containsOrDefault(JSONObject config, String name) {
        return containsOrDefault(config, name, (T) DEFAULTS.get(name));
    }

    @SuppressWarnings("unchecked")
    private static <T> T containsOrDefault(JSONObject config, String name, T defaultObject) {
        return (T) config.getOrDefault(name, defaultObject);
    }
}
