package afl;

import edu.cmu.sv.kelinci.Mem;
import instrumentor.InstrumentLoader;
import io.AFLConfig;
import io.MethodCallFormatter;
import method.*;
import method.callbacks.CSVMethodCallback;
import method.callbacks.EmptyMethodCallback;
import method.callbacks.LogMethodCallback;
import method.callbacks.MethodCallback;
import mock.TransformClassLoader;
import mock.answers.readers.ByteReaderList;
import mock.answers.readers.inputstream.ByteReaderInputStreamList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
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

    public static final byte STATUS_DONE = 5;
    static final byte STATUS_QUEUE_FULL = 3;
    static final byte STATUS_COMM_ERROR = 4;
    private static final byte STATUS_SUCCESS = 0;
    private static final byte STATUS_TIMEOUT = 1;
    private static final byte STATUS_CRASH = 2;
    private static final String DEFAULT_FORMAT = "[%4$s:%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS|%2$s] %5$s%6$s%n";
    private static final int DEFAULT_PORT = 7007;
    private static final String DEFAULT_RUN_ONCE = null;
    private static boolean isRunning = true;
    private static AFLConfig CONFIG;
    private static Logger LOGGER = Logger.getLogger(Kelinci.class.getName());
    private static MethodCallSession methodCallSession;
    private static TransformClassLoader transformClassLoader;
    private static ByteReaderInputStreamList byteReaderList;
    private static AFLServer aflServer;

    /**
     * Method to run in a thread handling one request from the queue at a time.
     * <p>
     * LOCAL_MODE means you only send over a path to the input file.
     * DEFAULT_MODE means the actual bytes of the file are sent.
     */
    private static void doFuzzerRuns() {
        LOGGER.log(Level.INFO, "Fuzzer runs handler thread started.");
        ExecutorService service = getExecutorService();

        while (isRunning) {
            try {
                FuzzRequest request = aflServer.poll();
                if (request != null) {
                    int result = request.getResult();
                    Mem.clear();
                    if (request.fileRequest != null) {
                        //Set up callbacks to read from file
                        LOGGER.log(Level.INFO, "Starting fuzz request");
                        OutputStream os = request.clientSocket.getOutputStream();
                        if (result != STATUS_COMM_ERROR) {
                            result = runMethodCall(service, new File(request.fileRequest));
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

    private static int runMethodCall(ExecutorService service, File file) throws IOException {
        // run app with input loads byte readers with input file
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        System.out.println(byteReaderList);
        byteReaderList.setInputStream(inputStream);
        LOGGER.log(Level.INFO, "Starting " + methodCallSession);
        MethodData methodData = methodCallSession.runMethod(service, CONFIG.timeout);
        Throwable e = methodData.getReturnException();
        if (e != null) {
            if (e instanceof TimeoutException) {
                LOGGER.log(Level.WARNING, "Time-out!");
                return STATUS_TIMEOUT;
            }
            Throwable throwable = e;
            if (e.getCause() != null) {
                throwable = e.getCause();
            }
            if (throwable instanceof RuntimeException) {
                LOGGER.log(Level.WARNING, "RuntimeException thrown!", throwable);
            } else {
                LOGGER.log(Level.WARNING, "Uncaught throwable!", e);
            }
            return STATUS_CRASH;
        }
        LOGGER.log(Level.INFO, "Finished!");
        return STATUS_SUCCESS;
    }

    public static ExecutorService getExecutorService() {
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

    private static void createRunAFLFile() throws IOException {
        File file = new File("./run_afl.sh");
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("Can't delete old run_afl.sh file");
            }
            if (!file.createNewFile()) {
                throw new RuntimeException("Run AFL file not created");
            }
            file.deleteOnExit();
        }
        OutputStream outputStream = new FileOutputStream(file);
        String s = "#!/bin/bash\n" +
                "\n" +
                "afl-fuzz -t " + (CONFIG.timeout + 1000) + " -i in_dir -o out_dir ./fuzzerside/interface @@";
        outputStream.write(s.getBytes());
        outputStream.flush();
        outputStream.close();
        if (!file.setExecutable(true, false)) {
            throw new RuntimeException("Can't set run AFL file executable");
        }
    }

    public static void main(String args[]) throws InterruptedException {

        /*
         * Parse command line parameters: load the main class,
         * grab -port option and store command-line parameters for fuzzing runs.
         */
        if (args.length < 2) {
            System.err.println(
                    "Usage: java afl.Kelinci [-i <input dir / jar>] [-l <libs dir> [-p N] [-r Path] <instrumented_dir> <config>");
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
        String runOnceFile = DEFAULT_RUN_ONCE;
        File inputSource = null;
        File libs = null;

        int curArg = 0;
        label:
        while (args.length > curArg) {
            switch (args[curArg]) {
                case "-i":
                case "-input":
                    inputSource = new File(args[curArg + 1]);
                    curArg += 2;
                    break;
                case "-l":
                case "-libs":
                    libs = new File(args[curArg + 1]);
                    curArg += 2;
                    break;
                case "-p":
                case "-port":
                    port = Integer.parseInt(args[curArg + 1]);
                    curArg += 2;
                    break;
                case "-r":
                case "-runOnce":
                    runOnceFile = args[curArg + 1];
                    curArg += 2;
                    break;
                default:
                    break label;
            }
        }
        File instrumentedDir = new File(args[curArg]);
        curArg++;

        //TODO: Parse config first and instrument classes with ByteBuddy
        /*
         * Parse methodcall definition and config
         */
        byteReaderList = new ByteReaderInputStreamList(LOGGER);
        try {
            transformClassLoader = new TransformClassLoader(instrumentedDir.getPath());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return;
        }
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
                MethodCall methodCall = MethodCallParser.setupMethodCall(LOGGER, transformClassLoader, (JSONObject) jsonObject.get("definition"),
                        byteReaderList);
                if (methodCall == null) {
                    return;
                }
                LOGGER.log(Level.INFO, "Parsing Finished");
                methodCallSession = methodCall.createSession(EmptyMethodCallback.create());
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error can't start the fuzzer because class " + e.getMessage() + " not found");
            return;
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.SEVERE, "Error can't start the fuzzer because field " + e.getMessage() + " not found");
            return;
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Error can't start the fuzzer because method " + e.getMessage() + " not found");
            return;
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Config Parse error");
            return;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IO Parse error");
            return;
        } catch (MethodCallParser.MethodCallConfigKeyException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "Other exception");
            return;
        }
        //Loads instrumented classes to classpath and creates instrumented classes if necessary
        if (!InstrumentLoader.loadInstrumentedClasses(inputSource, libs, instrumentedDir)) {
            System.exit(1);
        }

        //Setup the AFLServer to get requests from interface program
        aflServer = new AFLServer(port);

        if (methodCallSession == null) {
            return;
        }

        MethodCallback methodCallback = LogMethodCallback.create(LOGGER, true).link(byteReaderList);
        if (CONFIG.logToCSV != null) {
            try {
                methodCallback = methodCallback.link(CSVMethodCallback.create(CONFIG.logToCSV));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e, () -> "Couldn't link CSVMethodCallback continuing without it");
            }
        }
        methodCallSession.linkMethodCallback(methodCallback);
        if (runOnceFile != null) {
            int exitStatus = 0;
            ExecutorService service = getExecutorService();
            File file = new File(runOnceFile);
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
                    exitStatus = runMethodCall(service, file);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error reading from file " + file.getAbsolutePath());
                    e.printStackTrace();
                } finally {
                    LOGGER.log(Level.INFO, "Service Shutting Down");
                    service.shutdownNow();
                }
            }
            LOGGER.log(Level.INFO, "Method Call Complete");
            System.exit(exitStatus);
            return;
        }

        try {
            createRunAFLFile();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Can't create run_afl.sh file ", e);
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
        try {
            System.in.read();
            aflServer.stop(false);
            isRunning = false;
            while (server.isAlive()) {
                Thread.sleep(500);
            }
            while (fuzzerThread.isAlive()) {
                Thread.sleep(500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File("./run_afl.sh");
        if (!file.delete()) {
            LOGGER.log(Level.WARNING, "Couldn't Delete run_afl.sh file");
        }
    }
}
