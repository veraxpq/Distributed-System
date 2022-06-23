package user;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * This class is for the user's socket which contains the user's information and thread for future communication
 */
public class UserSocket {
    private final String host;
    private final int port;
    private Socket socket;
    private Thread accessThread;
    private String userName;
    private static final Logger LOGGER = Logger.getLogger(UserSocket.class.getName());

    /**
     * User socket constructor
     * @param host socket host to connect to
     * @param port socket port to connect to
     */
    public UserSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Establish connection to socket.
     * @param userName New user's name
     * @param runnable New user's chat controller
     * @return able to connect or not
     */
    public boolean connectSocket(String userName, Runnable runnable) {
        try {
            this.userName = userName;
            // Establish connection to socket server
            LOGGER.info("Connecting...");
            this.socket = new Socket(this.host, this.port);
            Thread.sleep(1000);
            LOGGER.info("Connected!");

            // Get Socket output stream
            PrintStream output = new PrintStream(this.socket.getOutputStream());

            // send nickname to server
            output.println(userName);

            this.accessThread = new Thread(runnable);
            this.accessThread.start();
            return true;
        } catch (IOException | InterruptedException ex) {
            LOGGER.warning("Something went wrong while connecting to server");
            return false;
        }
    }

    /**
     * Get username
     * @return username
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Set username
     * @param userName new username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get user's thread
     * @return user's thread
     */
    public Thread getAccessThread() {
        return this.accessThread;
    }

    /**
     * Get user's socket
     * @return user's socket
     */
    public Socket getSocket() {
        return this.socket;
    }
}