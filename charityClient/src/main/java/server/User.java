package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * This is the user object class for each client that store the individual information including the userSocket
 */
public class User {
    private static int nbUser = 0;
    private final PrintStream streamOut;
    private final InputStream streamIn;
    private final String userName;
    private final Socket client;
    private final String color;
    private String room = "Home";
    private String balance;

    /**
     * User class constructor
     * @param client user's socket
     * @param name username
     * @throws IOException IOException
     */
    public User(Socket client, String name) throws IOException {
        this.client = client;
        this.streamOut = new PrintStream(this.client.getOutputStream());
        this.streamIn = this.client.getInputStream();
        this.userName = name;
        int userId = nbUser;
        this.color = ColorInt.getColor(userId);
        this.balance = "1000";
        nbUser += 1;
    }

    /**
     * Get output stream for user
     * @return output stream
     */
    public PrintStream getOutStream() {
        return this.streamOut;
    }

    /**
     * Get input stream for user
     * @return input stream
     */
    public InputStream getInputStream() {
        return this.streamIn;
    }

    /**
     * Get user's socket
     * @return user's socket
     */
    public Socket getClient() {
        return this.client;
    }

    /**
     * Check if socket is closed
     * @return Socket closed or not
     */
    public boolean isSocketClosed() {
        return this.client.isClosed();
    }

    /**
     * Get current room for the user
     * @return current room
     */
    public String getRoom() {
        return this.room;
    }

    /**
     * Set current for the user
     * @param room current room
     */
    public void setRoom(String room) {
        this.room = room;
    }

    /**
     * Reset the room for the user
     */
    public void resetRoom() {
        this.room = "HOME";
    }

    /**
     * Get the user's name
     * @return username
     */
    public String getUsername() {
        return this.userName;
    }

    /**
     * Get tht user's color
     * @return user's color
     */
    public String getColor() {
        return this.color;
    }

    /**
     * Get the user's balance
     * @return user's balance
     */
    public String getBalance() {
        return balance;
    }

    /**
     * Set the user's balance
     * @param balance user's balance
     */
    public void setBalance(String balance) {
        this.balance = balance;
    }
}
