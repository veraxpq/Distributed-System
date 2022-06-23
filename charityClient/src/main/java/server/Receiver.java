package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * This is receiver class that handle the functionality for input command line
 */
public class Receiver implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Receiver.class.getName());
    private final User user;
    private final Server server;
    private String room = "";

    /**
     * Receiver class constructor
     * @param server current server object
     * @param user   current user connection
     */
    public Receiver(Server server, User user) {
        this.server = server;
        this.user = user;
    }

    /**
     * Wait for user to send message and show it to other users
     */
    @Override
    public void run() {
        try {
            Scanner in = new Scanner(this.user.getInputStream());

            // While socket connection is still active
            while (!this.user.isSocketClosed()) {
                if (in.hasNextLine()) {
                    // Decode JSON created by user
                    JSONObject json = (JSONObject) new JSONParser().parse(in.nextLine());
                    String message = (String) json.get("message");

                    // Send message
                    Command command = new Command(message);
                    JSONObject jsonReturn;
                    if (command.isCommand()) {
                        if (command.isSync()) {
                            // /sync donate
                            command.setDM(false);
                            if (message.split(" ")[1].equals("donate")) {
                                jsonReturn = new JSONObject();
                                jsonReturn.put("balance", user.getBalance());
                                sendMessage(jsonReturn.toString(), this.user);
                            }
                        } else if (command.isTrans()) {
                            String[] arr = message.split(" ");
                            String userName = arr[1];
                            synchronized (this.server) {
                                int donateAmount = Integer.parseInt(user.getBalance()) - Integer.parseInt(arr[2]);
                                user.setBalance(arr[2]);
                                jsonReturn = new JSONObject();
                                jsonReturn.put("balance", arr[2]);
                                sendMessage(jsonReturn.toString(), this.user);
                                String roomHost = this.server.getRoomHost(user.getRoom());

                                for (User user : server.getAllThreads()) {
                                    if (user.getUsername().equals(roomHost)) {
                                        user.setBalance(String.valueOf(Integer.parseInt(user.getBalance()) + donateAmount));
                                        jsonReturn.put("balance", user.getBalance());
                                        sendMessage(jsonReturn.toString(), user);
                                    }
                                }
                            }
                        } else if (command.isEnterRoom()) { // Get second word of command to change room name
                            this.room = message.split(" ")[1];
                            this.user.setRoom(this.room);
                            this.server.addNewUserToRoom(this.room, this.user.getUsername());
                            jsonReturn = new JSONObject();
                            jsonReturn.put("enterRoom", this.room);
                            jsonReturn.put("host", getHost());
                            // jsonReturn.put("balance", this.user.getBalance());
                            LOGGER.info("--- " + jsonReturn.toString());
                            sendMessage(jsonReturn.toString(), this.user);

                            // Show join message
                            for (User thread : this.server.getThreadsByRoom(this.room)) {
                                jsonReturn = new JSONObject();
                                jsonReturn.put("message", this.user.getUsername() + " joined the room");
                                sendMessage(jsonReturn.toString(), thread);
                            }

                            // Update User List
                            List<User> syncUserListInTargetRoom = this.server.getThreadsByRoom(this.room);
                            StringBuilder userListMessage = new StringBuilder("Current online users in room " + this.room + ":");

                            for (User user : syncUserListInTargetRoom) {
                                userListMessage.append("\n\t").append(user.getUsername());
                            }

                            jsonReturn = new JSONObject();
                            jsonReturn.put("user_list", userListMessage.toString());

                            for (User user : syncUserListInTargetRoom) {
                                sendMessage(jsonReturn.toString(), user);
                            }
                        } else if (command.isLeaveRoom()) {
                            this.room = message.split(" ")[1];
                            this.server.removeUserFromRoom(this.room, this.user.getUsername());
                            this.user.resetRoom();
                            jsonReturn = new JSONObject();
                            jsonReturn.put("leaveRoom", this.room);
                            LOGGER.info("--- " + jsonReturn.toString());
                            sendMessage(jsonReturn.toString(), this.user);

                            // Show leave message
                            for (User thread : this.server.getThreadsByRoom(this.room)) {
                                jsonReturn = new JSONObject();
                                jsonReturn.put("message", this.user.getUsername() + " left the room");
                                sendMessage(jsonReturn.toString(), thread);
                            }

                            // Update user list & host
                            List<User> syncUserListInTargetRoom = this.server.getThreadsByRoom(this.room);
                            StringBuilder userListMessage = new StringBuilder("Current online users in room " + this.room + ":");

                            for (User user : syncUserListInTargetRoom) {
                                userListMessage.append("\n\t").append(user.getUsername());
                            }

                            jsonReturn = new JSONObject();
                            jsonReturn.put("host", this.server.getRoomHost(this.room));
                            jsonReturn.put("user_list", userListMessage.toString());

                            for (User user : syncUserListInTargetRoom) {
                                sendMessage(jsonReturn.toString(), user);
                            }
                        } else if (command.isOffline()) {
                            // Get the username that would be removed from the server list
                            String offlineUsername = message.split(" ")[1];
                            server.removeUser(offlineUsername);
                        } else if (command.isDM()) {
                            try {
                                String[] splitMessage = message.split(" ");
                                String userMessage = splitMessage[2];
                                jsonReturn = new JSONObject();
                                User selectedUser = this.server.getThreadByName(splitMessage[1]);
                                if (selectedUser != null) {
                                    jsonReturn.put("message", userMessage);
                                    sendMessage(jsonReturn.toString(), selectedUser);
                                } else {
                                    jsonReturn.put("message", "User does not exist or is not online");
                                    sendMessage(jsonReturn.toString(), this.user);
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                jsonReturn = new JSONObject();
                                jsonReturn.put("message", "Invalid dm format. Correct format: /dm {user_name} {message}");
                                sendMessage(jsonReturn.toString(), this.user);
                            }
                        } else if (command.checkExist()) {
                            jsonReturn = new JSONObject();
                            boolean found = false;
                            int num = 0;
                            String username = message.split(" ")[1];
                            for (User user: this.server.getAllThreads()){
                                if (user.getUsername().equals(username)){
                                    found = true;
                                    num = Integer.parseInt(user.getBalance());
                                    break;
                                }
                            }

                            if (found){
                                LOGGER.info("The user exist in system, the balance is: " + num);
                                jsonReturn.put("exist", "yes");
                                jsonReturn.put("balance", String.valueOf(num));
                            } else {
                                LOGGER.info("New user in system");
                                jsonReturn.put("exist", "no");
                            }
                            sendMessage(jsonReturn.toString(), this.user);
                        }
                    } else {
                        // Normal message
                        if (this.room.equals("")) {
                            jsonReturn = new JSONObject();
                            jsonReturn.put("message", "You're currently in no room, change room by typing /room {room_name})");
                            sendMessage(jsonReturn.toString(), this.user);
                        } else {
                            String date = new SimpleDateFormat("h:mm a").format(new Date());
                            List<User> userList = this.server.getThreadsByRoom(this.room);
                            for (User thread : userList) {
                                jsonReturn = new JSONObject();
                                jsonReturn.put("username", this.user.getUsername());
                                jsonReturn.put("user_color", this.user.getColor());
                                jsonReturn.put("message", message);
                                jsonReturn.put("time", date);

                                sendMessage(jsonReturn.toString(), thread);
                            }
                        }
                    }
                }
            }
        } catch (ParseException | IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * @param message message to send
     * @param thread  to what user/thread the message should be sent
     */
    public static void sendMessage(String message, User thread) {
        PrintStream userOut = thread.getOutStream();
        if (userOut != null) {
            userOut.println(message);
            userOut.flush();
        }
    }

    /**
     * Return the host of the designated room
     * @return
     */
    private String getHost(){
       return this.server.getRoomHost(this.room);
    }
}