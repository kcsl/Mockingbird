package afl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static afl.Kelinci.STATUS_QUEUE_FULL;

/**
 * @author Derrick Lockwood
 * @created 6/24/18.
 */
public class AFLServer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(AFLServer.class.getName());
    private static final Queue<FuzzRequest> requestQueue = new ConcurrentLinkedQueue<>();
    private static final int maxQueue = 10;

    static {
        LOGGER.setParent(Logger.getLogger(Kelinci.class.getName()));
    }

    private final File tmpfile;
    private final int port;
    private boolean isRunning = true;
    private boolean completed = false;

    public AFLServer(int port) {
        this.port = port;
        try {
            tmpfile = File.createTempFile("kelinci-input", "");
            tmpfile.deleteOnExit();
        } catch (IOException ioe) {
            throw new RuntimeException("Error creating tmp file");
        }
    }

    public FuzzRequest poll() {
        return requestQueue.poll();
    }

    public void stop() {
        stop(true);
    }

    public void stop(boolean block) {
        isRunning = false;
        if (block) {
            while (!completed) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port)) {
            LOGGER.log(Level.INFO, "Server listening on port " + port);
            ss.setSoTimeout(1000);
            while (isRunning) {
                Socket s;
                try {
                    s = ss.accept();
                } catch (SocketTimeoutException e) {
                    continue;
                }
                LOGGER.log(Level.INFO, "Connection established.");

                boolean status = false;
                if (requestQueue.size() < maxQueue) {
                    LOGGER.log(Level.INFO, "Handling request 1 of " + (requestQueue.size() + 1));
                    status = requestQueue.offer(new FuzzRequest(tmpfile, s));
                    LOGGER.log(Level.INFO, "Request added to queue: " + status);
                }
                if (!status) {
                    LOGGER.log(Level.WARNING, "Queue full.");
                    OutputStream os = s.getOutputStream();
                    os.write(STATUS_QUEUE_FULL);
                    os.flush();
                    s.shutdownOutput();
                    s.shutdownInput();
                    s.close();
                    LOGGER.log(Level.WARNING, "Connection closed.");
                }
            }
        } catch (BindException be) {
            LOGGER.log(Level.SEVERE, "Unable to bind to port " + port);
            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception in request server");
            e.printStackTrace();
            System.exit(1);
        } finally {
            completed = true;
        }
    }
}
