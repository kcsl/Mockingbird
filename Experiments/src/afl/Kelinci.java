package afl;

import edu.cmu.sv.kelinci.Mem;
import instrumentor.Options;
import io.AFLConfig;
import io.MethodCallFormatter;
import method.MethodCall;
import method.MethodCallParser;
import method.MethodData;
import method.callbacks.LogMethodCallback;
import mock.answers.readers.ByteReaderList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.reflect.Reflection;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rodykers
 */
public class Kelinci {

    public static final byte STATUS_SUCCESS = 0;
    public static final byte STATUS_TIMEOUT = 1;
    public static final byte STATUS_CRASH = 2;
    public static final byte STATUS_QUEUE_FULL = 3;
    public static final byte STATUS_COMM_ERROR = 4;
    public static final byte STATUS_DONE = 5;

    public static final String DEFAULT_FORMAT = "[%4$s:%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS|%2$s] %5$s%6$s%n";
    public static final int DEFAULT_PORT = 7007;
    public static final String DEFAULT_RUN_ONCE = null;
    private static AFLConfig CONFIG;
    private static Logger LOGGER = Logger.getLogger(Kelinci.class.getName());
    private static MethodCall methodCall;
    private static ByteReaderList byteReaderList;
    private static AFLServer aflServer;

    public static byte result;

    /**
     * Method to run in a thread handling one request from the queue at a time.
     * <p>
     * LOCAL_MODE means you only send over a path to the input file.
     * DEFAULT_MODE means the actual bytes of the file are sent.
     */
    private static void doFuzzerRuns() {
        LOGGER.log(Level.INFO, "Fuzzer runs handler thread started.");
        ExecutorService service = getExecutorService();

        while (true) {
            try {
                FuzzRequest request = aflServer.poll();
                if (request != null) {
                    Mem.clear();
                    if (request.fileRequest != null) {
                        //Set up callbacks to read from file
                        OutputStream os = request.clientSocket.getOutputStream();
                        if (result != STATUS_COMM_ERROR) {
                            runMethodCall(service, new File(request.fileRequest));
                        }
                        LOGGER.log(Level.INFO, "Result: " + result);
                        LOGGER.log(Level.FINE, Mem.print());
                        // send back status
                        os.write(result);

                        // send back "shared memory" over TCP
                        os.write(Mem.mem, 0, Mem.mem.length);

                        // close connection
                        os.flush();
                        request.clientSocket.shutdownOutput();
                        request.clientSocket.shutdownInput();
                        request.clientSocket.setSoLinger(true, 100000);
                        request.clientSocket.close();
                        LOGGER.log(Level.INFO, "Connection closed.");
                    }
                } else {
                    // if no request, close your eyes for a bit
                    Thread.sleep(100);
                }
            } catch (SocketException se) {
                // Connection was reset, most probably means AFL process was killed.
                LOGGER.log(Level.WARNING, "Connection reset.");
            } catch (Exception e) {
                service.shutdownNow();
                e.printStackTrace();
                throw new RuntimeException("Exception running fuzzed input");
            }
        }
    }

    private static void runMethodCall(ExecutorService service, File file) throws IOException {
        // run app with input loads byte readers with input file
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        byteReaderList.setInputStream(inputStream);
        LOGGER.log(Level.INFO, "Starting " + methodCall);
        MethodData methodData = methodCall.runMethod(service, CONFIG.timeout, CONFIG.refreshObjects);
        Exception e = methodData.getReturnException();
        if (e instanceof TimeoutException) {
            LOGGER.log(Level.WARNING, "Time-out!");
            result = STATUS_TIMEOUT;
        } else if (e != null) {
            if (e.getCause() instanceof RuntimeException) {
                LOGGER.log(Level.WARNING, "RuntimeException thrown!");
            } else if (e.getCause() instanceof Error) {
                LOGGER.log(Level.WARNING, "Error thrown!");
            } else {
                LOGGER.log(Level.WARNING, "Uncaught throwable!");
            }
            result = STATUS_CRASH;
        } else {
            LOGGER.log(Level.INFO, "Finished!");
            result = STATUS_SUCCESS;
        }
    }

    private static ExecutorService getExecutorService() {
        //Creates the deamon thread such that the JVM can close instead of have to wait for the JVM to close
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            private ThreadFactory threadFactory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = threadFactory.newThread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public static void main(String args[]) throws InterruptedException{

        /*
         * Parse command line parameters: load the main class,
         * grab -port option and store command-line parameters for fuzzing runs.
         */
        if (args.length < 2) {
            System.err.println("Usage: java afl.Kelinci [-i <input dir / jar>] [-p N] [-r Path] <instrumented_dir> <config>");
            return;
        }

        int port = DEFAULT_PORT;
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setFilter(null);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(LOGGER.getLevel());
        consoleHandler.setFormatter(new MethodCallFormatter(DEFAULT_FORMAT));
        LOGGER.addHandler(consoleHandler);
        String runOnce = DEFAULT_RUN_ONCE;
        File inputSource = null;

        int curArg = 0;
        label:
        while (args.length > curArg) {
            switch (args[curArg]) {
                case "-i":
                case "-input":
                    inputSource = new File(args[curArg + 1]);
                    curArg += 2;
                    break;
                case "-p":
                case "-port":
                    port = Integer.parseInt(args[curArg + 1]);
                    curArg += 2;
                    break;
                case "-r":
                case "-runOnce":
                    runOnce = args[curArg + 1];
                    curArg += 2;
                    break;
                default:
                    break label;
            }
        }
        File instrumentedDir = new File(args[curArg]);
        curArg++;
        if (!InstrumentLoader.loadInstrumentedClasses(inputSource, instrumentedDir)){
            return;
        }


        //Setup the AFLServer to get requests from interface program
        aflServer = new AFLServer(port);

        /*
         * Parse methodcall definition and config
         */
        byteReaderList = new ByteReaderList(LOGGER);
        String configFile = args[curArg];
        LOGGER.log(Level.INFO, "Parsing " + configFile);
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(configFile));
            if (jsonObject.containsKey("config")) {
                CONFIG = new AFLConfig((JSONObject) jsonObject.get("config"));
            } else {
                CONFIG = new AFLConfig();
            }
            consoleHandler.setLevel(CONFIG.consoleLevel);
            if (CONFIG.byteReaderOutOnly) {
                consoleHandler.setFilter(ByteReaderList.getFilter());
                consoleHandler.setFormatter(ByteReaderList.getFormatter());
            }
            if (CONFIG.logFile != null) {
                FileHandler fileHandler = new FileHandler();
                fileHandler.setLevel(CONFIG.fileLevel);
                fileHandler.setFormatter(new MethodCallFormatter(DEFAULT_FORMAT));
                LOGGER.addHandler(fileHandler);
            }
            if (jsonObject.containsKey("definition")) {
                methodCall = MethodCallParser.setupMethodCall(LOGGER, (JSONObject) jsonObject.get("definition"),
                        byteReaderList.getByteReaders());
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error can't start the fuzzer because class " + e.getMessage() + " not found");
            methodCall = null;
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.SEVERE, "Error can't start the fuzzer because field " + e.getMessage() + " not found");
            methodCall = null;
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Error can't start the fuzzer because method " + e.getMessage() + " not found");
            methodCall = null;
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Config Parse error");
            methodCall = null;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IO Parse error");
            methodCall = null;
        } catch (MethodCallParser.MethodCallConfigKeyException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "Other exception");
            methodCall = null;
        }
        LOGGER.log(Level.INFO, "Parsing Finished");

        if (methodCall == null)
            return;

        methodCall.linkMethodCallback(LogMethodCallback.create(LOGGER, true).link(byteReaderList));
        if (runOnce != null) {
            ExecutorService service = getExecutorService();
            File file = new File(runOnce);
            if (file.isDirectory()) {
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (!f.getName().startsWith(".")) {
                        LOGGER.log(Level.INFO, "Running file " + f.getAbsolutePath());
                        try {
                            runMethodCall(service, f);
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Error reading from file " + f);
                            e.printStackTrace();
                        }
                    }
                }
                LOGGER.log(Level.INFO, "Service Shutting Down");
                service.shutdownNow();
            } else {
                LOGGER.log(Level.INFO, "Running Once on file " + file.getAbsolutePath());
                try {
                    runMethodCall(service, file);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error reading from file " + file.getAbsolutePath());
                    e.printStackTrace();
                } finally {
                    LOGGER.log(Level.INFO, "Service Shutting Down");
                    service.shutdownNow();
                }
            }
            LOGGER.log(Level.INFO, "Method Call Complete");
            return;
        }

        /*
         * Handle requests for fuzzer runs in separate thread.
         */
        Thread fuzzerThread = new Thread(Kelinci::doFuzzerRuns);

        /*
         * Start the server thread
         */
        Thread server = new Thread(aflServer);

        server.start();
        fuzzerThread.start();
        while (fuzzerThread.isAlive()) {
            Thread.sleep(500);
        }
        fuzzerThread.interrupt();
        System.exit(0);
    }
}
