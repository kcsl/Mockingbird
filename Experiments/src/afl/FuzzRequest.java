package afl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static afl.Kelinci.STATUS_COMM_ERROR;
import static afl.Kelinci.result;

/**
 * @author Derrick Lockwood
 * @created 6/24/18.
 */
public class FuzzRequest {
    public static final byte DEFAULT_MODE = 0;
    public static final byte LOCAL_MODE = 1;
    private static final Logger LOGGER = Logger.getLogger(FuzzRequest.class.getName());

    public final Socket clientSocket;
    public final String fileRequest;
    private final File tmpfile;

    static {
        LOGGER.setParent(Logger.getLogger(Kelinci.class.getName()));
    }

    public FuzzRequest(File tmpFile, Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        fileRequest = getFileRequestFromServer(clientSocket);
        this.tmpfile = tmpFile;
    }

    private String getFileRequestFromServer(Socket request) throws IOException {

        InputStream is = request.getInputStream();

        // read the mode (local or default)
        byte mode = (byte) is.read();

        /* LOCAL MODE */
        if (mode == LOCAL_MODE) {
            LOGGER.log(Level.INFO, "Handling request in LOCAL MODE.");

            // read the length of the path (integer)
            int pathlen = is.read() | is.read() << 8 | is.read() << 16 | is.read() << 24;
            LOGGER.log(Level.FINE, "Path len = " + pathlen);

            if (pathlen < 0) {
                LOGGER.log(Level.SEVERE, "Failed to read path length");
                result = STATUS_COMM_ERROR;
            } else {

                // read the path
                byte input[] = new byte[pathlen];
                int read = 0;
                while (read < pathlen) {
                    if (is.available() > 0) {
                        input[read++] = (byte) is.read();
                    } else {
                        LOGGER.log(Level.SEVERE, "No input available from stream, strangely, breaking.");
                        result = STATUS_COMM_ERROR;
                        break;
                    }
                }
                String path = new String(input);
                LOGGER.log(Level.INFO, "Received path: " + path);

                return path;
            }

            /* DEFAULT MODE */
        } else {
            LOGGER.log(Level.INFO, "Handling request in DEFAULT MODE.");

            // read the size of the input file (integer)
            int filesize = is.read() | is.read() << 8 | is.read() << 16 | is.read() << 24;
            LOGGER.log(Level.FINE, "File size = " + filesize);

            if (filesize < 0) {
                LOGGER.log(Level.SEVERE, "Failed to read file size");
                result = STATUS_COMM_ERROR;
            } else {

                // read the input file
                byte input[] = new byte[filesize];
                int read = 0;
                while (read < filesize) {
                    if (is.available() > 0) {
                        input[read++] = (byte) is.read();
                    } else {
                        LOGGER.log(Level.WARNING, "No input available from stream, strangely");
                        LOGGER.log(Level.WARNING, "Appending a 0");
                        input[read++] = 0;
                    }
                }
                try (FileOutputStream stream = new FileOutputStream(tmpfile)) {
                    stream.write(input);
                    stream.close();
                    return tmpfile.getAbsolutePath();
                } catch (IOException ioe) {
                    throw new RuntimeException("Error writing to tmp file");
                }
            }
        }
        return null;
    }
}
