package server;

import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;

/**
 * This is the main server class that setup the Charity Now server.
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private final int port;
    private static final List<User> allThreads = new ArrayList<>();
    private HashSet<String> userSet = new HashSet<>();
    private HashMap<String, List<String>> roomMap = new HashMap<>();
    public static HashMap<String, User> userMap = new HashMap();

    /**
     * @param port port the socket should connect to.
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Start socket server
     */
    public void startServer() {
        try {
            // Create socket connection
            ServerSocket socketServer = new ServerSocket(this.port);
            acceptUsers(socketServer);
        } catch (IOException e) {
            LOGGER.warning("Could not connect to port: " + this.port);
            System.exit(1);
        }
    }

    /**
     * Grant access to new users
     *
     * @param serverSocket current server socket connection
     */
    private void acceptUsers(ServerSocket serverSocket) throws IOException {
        // Show current socket address information
        LOGGER.info("Server port: " + serverSocket.getLocalSocketAddress());
        while (true) {
            try {
                // Accept new user
                Socket socket = serverSocket.accept();
                String username = (new Scanner(socket.getInputStream())).nextLine();
                username = username.replace(",", "").replace(" ", "_");

                if (ifUsernameDup(username)) {
                    popAlert();
                    socket.close();
                    continue;
                }

                LOGGER.info("New Client: \"" + username + "\"\n\t     Host:" + socket.getRemoteSocketAddress());

                // create new User
                User newUser = new User(socket, username);
                userSet.add(username);

                // Add user to User list
                this.allThreads.add(newUser);

                // Create a new thread incoming message of new user
                new Thread(new Receiver(this, newUser)).start();

                // Send welcome message
                JSONObject jsonReturn = new JSONObject();
                jsonReturn.put("message", "Welcome to the server!");
                newUser.getOutStream().println(jsonReturn.toString());
            } catch (IOException ex) {
                LOGGER.warning("Failed accepting new user on port: " + this.port);
            }
        }
    }

    /**
     * Get all users
     * @return List of users
     */
    public List<User> getAllThreads() {
        return this.allThreads;
    }

    /**
     * Get user by username
     * @param userName username
     * @return user
     */
    public User getThreadByName(String userName) {
        for (User user : this.allThreads) {
            if (user.getUsername().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get all users by room
     * @param room room
     * @return List of users
     */
    public static List<User> getThreadsByRoom(String room) {
        List<User> threads = new ArrayList<>();
        for (User user : allThreads) {
            if (user.getRoom().equals(room)) {
                threads.add(user);
            }
        }
        return threads;
    }

    /**
     * Get the map for room-users pair
     * @return Room-users map
     */
    public HashMap<String, List<String>> getRoomMap() {
        return this.roomMap;
    }

    /**
     * Get the host for the room
     * @param room room
     * @return host for the room
     */
    public String getRoomHost(String room) {
        return this.roomMap.get(room).size() != 0 ? this.roomMap.get(room).get(0) : null;
    }

    /**
     * Get current users' set
     * @return current users' set
     */
    public HashSet<String> getUserSet() {
        return userSet;
    }

    /**
     * Add a new user to the room
     * @param room room
     * @param username username
     */
    public void addNewUserToRoom(String room, String username) {
        if (this.roomMap.get(room) == null) {
            List<String> newUserList = new ArrayList<String>();
            newUserList.add(username);
            this.roomMap.put(room, newUserList);
        } else {
            this.roomMap.get(room).add(username);
        }
    }

    /**
     * Remove the user from the given room
     * @param room room
     * @param username username
     */
    public void removeUserFromRoom(String room, String username) {
        this.roomMap.get(room).remove(username);
    }

    /**
     * Return if the username has existed in the allThread list
     * @param name username
     * @return has the username existed or not
     */
    public boolean ifUsernameDup(String name) {
        return userSet.contains(name);
    }

    /**
     * Remove user from the userSet
     * @param userName username
     * @throws IOException IOException
     */
    public void removeUser(String userName) throws IOException {
        if (!userSet.contains(userName)) {
            return;
        }
        userSet.remove(userName);
        for (int i = 0; i < allThreads.size(); i++) {
            if (allThreads.get(i).getUsername().equals(userName)) {
                allThreads.get(i).getClient().close();
                allThreads.remove(i);
                return;
            }
        }
    }

    /**
     * If the userName has already existed in the server, pops out an alert window
     */
    public void popAlert() {
        JFrame jFrame = new JFrame();
        JOptionPane.showMessageDialog(jFrame, "Your username has been used, please try again");
    }
}
