package guis;

import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import user.TextValidator;
import user.Main;
import user.UserSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * This is the chat controller for the client. It has all the chat page features.
 * Users are able to enter/leave a room, logout, send direct message or make donations.
 */
public class chatController implements Runnable {
    // Used to exhibit online users in the current room
    public VBox chatBox;
    public VBox userListBox;
    public TextField messageInput;
    public Label currentRoom;
    public Label currentBalance;
    public Label currentHost;
    public TextField targetRoomInput;
    public TextField DonationInput;
    public Button enterRoomButton;
    public Button leaveRoomButton;
    public Button logoutButton;

    public static final LinkedList<String> newMessages = new LinkedList<>();
    public ScrollPane chatScroll;
    public ScrollPane userListScroll;
    public static boolean messageWaiting = false;
    private transactionController transController = new transactionController();
    private static final Logger LOGGER = Logger.getLogger(chatController.class.getName());

    @FXML
    private UserSocket userSocket;

    // new fields
    private String hostName = null;
    public String userName = null;
    public long balance = 0;
    public int donateNum = 0;
    public boolean alreadyInit = false;

    /**
     * Push message being send into the linkedlist and set messageWaiting to be true
     * @param e action
     */
    public void messageSend(ActionEvent e) {
        if (messageInput.getText().length() >= 1) {
            if (this.userSocket.getAccessThread().isAlive()) {
                synchronized (this.newMessages) {
                    this.messageWaiting = true;
                    this.newMessages.push(messageInput.getText());
                }
            }
            messageInput.setText("");
            messageInput.requestFocus();
        }
    }

    /**
     * Run function for chat controller thread
     */
    @Override
    public void run() {
        try {
            Socket socket = this.userSocket.getSocket();
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), false);
            InputStream serverInStream = socket.getInputStream();
            Scanner serverIn = new Scanner(serverInStream);

            // While socket connection is still active
            while (!socket.isClosed()) {
                serverInStream = socket.getInputStream();
                // If there is a new message from other users
                if (serverInStream.available() > 0) {
                    if (serverIn.hasNextLine()) {
                        this.messageHandler(serverIn.nextLine());
                        chatBox.heightProperty().addListener(observable -> chatScroll.setVvalue(1D));
                        userListBox.heightProperty().addListener(observable -> userListScroll.setVvalue(1D));
                    }
                }

                // If there are new messages from current user
                if (this.messageWaiting) {
                    String nextSend = "";
                    synchronized (this.newMessages) {
                        nextSend = this.newMessages.pop();
                        this.messageWaiting = !this.newMessages.isEmpty();
                    }

                    // Output message
                    JSONObject json = new JSONObject();
                    json.put("message", nextSend);
                    json.put("timezone", TimeZone.getDefault().getID());

                    serverOut.println(json);
                    serverOut.flush();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Append message to the chatbox
     *
     * @param jsonString json string send from server
     */
    public void messageHandler(String jsonString) {
        Platform.runLater(() -> {
            try {
                JSONObject json = (JSONObject) new JSONParser().parse(jsonString);

                // Set values
                String username = (String) json.get("username");
                String userColor = (String) json.get("user_color");
                String enterRoom = (String) json.get("enterRoom");
                String leaveRoom = (String) json.get("leaveRoom");
                String message = (String) json.get("message");
                String time = (String) json.get("time");
                String host = (String) json.get("host");
                String account = json.get("balance") == null ? null : json.get("balance").toString();
                String onlineUseList = (String) json.get("user_list");
                String exist = (String) json.get("exist");

                if (exist != null ){
                    if (exist.equals("yes")) {
                        LOGGER.info("User exist in system, update balance");
                        currentBalance.setText(account);
                        this.balance = Integer.parseInt(account);
                    } else {
                         LOGGER.info("New user ---");
                    }
                }

                if (account != null) {
                    LOGGER.info("balance " + account);
                    this.balance = Integer.parseInt(account);
                    currentBalance.setText(account);
                }

                if (onlineUseList != null) {
                    setOnlineUserList(onlineUseList);
                }

                // Enter room
                if (enterRoom != null) {
                    currentRoom.setText(enterRoom);
                    hostName = host;
                    currentHost.setText(host);
                    return;
                }

                // Leave room
                if (leaveRoom != null) {
                    currentRoom.setText("");
                    currentHost.setText("");
                    return;
                }

                if (host != null) {
                    currentHost.setText(host);
                    return;
                }

                GridPane gridpane = new GridPane();
                GridPane gridpane2 = new GridPane();

                // Add Username
                if (username != null) {
                    Label name = new Label(username + ": ");
                    name.getStyleClass().add("username");
                    name.setTextFill(Color.web(userColor));
                    gridpane2.add(name, 1, 0);
                }

                // Add message
                TextValidator validator = new TextValidator();
                String messageEmoji = validator.replaceEmoji(message);
                TextArea textArea = new TextArea(messageEmoji);

                double width = (this.computeTextWidth(textArea.getFont(), textArea.getText()) + 10) + 20;
                textArea.setPrefWidth(width);

                int height = textArea.getText() == null ? 0 : (textArea.getText().split("\r\n|\r|\n").length * 17) + 14;
                textArea.setMinHeight(height);
                textArea.setMaxHeight(height);
                textArea.setEditable(false);
                textArea.getStyleClass().add("textArea");

                gridpane2.add(textArea, 2, 0);
                gridpane.add(gridpane2, 1, 0);

                // Add date
                if (time != null) {
                    Label date = new Label(time);
                    date.getStyleClass().add("date");
                    gridpane.add(date, 1, 1);
                }

                // Add pane
                Platform.runLater(() -> {
                    chatBox.getChildren().add(gridpane);
                });
            } catch (ParseException e) {
                LOGGER.warning(e.getMessage());
            }
        });
    }

    /**
     * Compute message box text width
     * @param font text font
     * @param text text
     * @return text width
     */
    private double computeTextWidth(Font font, String text) {
        Text helper = new Text();
        helper.setText(text);
        helper.setFont(font);
        helper.setWrappingWidth(0);
        double w = Math.min(helper.prefWidth(-1), 0.0D);
        helper.setWrappingWidth((int) Math.ceil(w));
        return Math.ceil(helper.getLayoutBounds().getWidth());
    }

    /**
     * Set user socket, including username, default current balance.
     * @param userSocket user's socket
     * @param name username
     */
    public void setUserSocket(UserSocket userSocket, String name) {
        this.userSocket = userSocket;
        this.userName = name;

        newMessages.push("/exist  " + this.userName);
        messageWaiting = true;
        this.currentBalance.setText("1000");
        this.balance = 1000;
    }


    /**
     * This function will collect the donation information and check the validity
     * if the info is valid, the transaction will proceed, otherwise, the alert will
     * display the error
     * @param e the click event
     * @throws IOException if any error
     */
    public void donateToHost(ActionEvent e) throws IOException {
        if (DonationInput.getText().length() >= 1) {
            try {
                donateNum = Integer.parseInt(DonationInput.getText());
                DonationInput.setText("");
                DonationInput.requestFocus();

            } catch (Exception err){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Donation should be a positive number");
                errorAlert.setContentText("Please re-enter.");
                errorAlert.showAndWait();
                DonationInput.setText("");
                DonationInput.requestFocus();
                this.donateNum = 0;
                return;
            }
            if (checkInput()) {
                openTransScreen(e);
            }
        }
    }

    /**
     * This function will open the transaction controller and sends
     * the donation info to transaction client
     * @param e the click event
     */
    protected void openTransScreen(ActionEvent e){
        // Initiate transaction screen
        transController.setup(this.balance,
                this.userName,this.hostName, donateNum
        );
        if (!alreadyInit) {
            transController.initiate();
            alreadyInit = true;
        }
        try {
            transController.prepareTrans();

            Platform.runLater(() -> {
                if(this.transController.isSuccess) {
                    this.transController.isSuccess = Boolean.parseBoolean(null);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * When connect button clicked, the user should be able to join into the target room
     * @param event
     * @throws IOException IOException
     */
    public void enterRoom(ActionEvent event) throws IOException, InterruptedException {
        if (targetRoomInput.getText().length() >= 1) {
            chatBox.getChildren().clear();

            if (this.userSocket.getAccessThread().isAlive()) {
                synchronized (this.newMessages) {
                    this.messageWaiting = true;
                    this.newMessages.push("/enter " + targetRoomInput.getText());
                }
            }
            messageInput.requestFocus();

            enterRoomButton.setDisable(true);
            leaveRoomButton.setDisable(false);
            enterRoomButton.setVisible(false);
            leaveRoomButton.setVisible(true);
            targetRoomInput.setDisable(true);
            logoutButton.setDisable(true);
        }
    }

    /**
     * When connect button clicked, the user should be able to join into the target room
     * @param event
     * @throws IOException IOException
     */
    public void leaveRoom(ActionEvent event) throws IOException, InterruptedException {
        if (targetRoomInput.getText().length() >= 1) {
            chatBox.getChildren().clear();
            userListBox.getChildren().clear();

            if (this.userSocket.getAccessThread().isAlive()) {
                synchronized (this.newMessages) {
                    this.messageWaiting = true;
                    this.newMessages.push("/leave " + targetRoomInput.getText());
                }
            }
            messageInput.requestFocus();

            enterRoomButton.setDisable(false);
            leaveRoomButton.setDisable(true);
            enterRoomButton.setVisible(true);
            leaveRoomButton.setVisible(false);
            targetRoomInput.setDisable(false);
            targetRoomInput.clear();
            logoutButton.setDisable(false);
        }
    }

    /**
     * When logout button clicked, the user should be able to jump to the home page.
     * @param event the click event
     * @throws IOException if any error
     */
    public void logout(ActionEvent event) throws IOException {
        try {
            synchronized (this.newMessages) {
                this.messageWaiting = true;
                this.newMessages.push("/offline " + userName);
            }
            Thread.sleep(1000);
            Parent root = FXMLLoader.load(getClass().getResource("/resources/homeScreen.fxml"));
            Main.stage.setScene(new Scene(root, Main.stageWidth, Main.stageHeight));
            Main.stage.show();
        } catch (IOException | InterruptedException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * This function will check the donation num and will display
     * the error message if anything wrong
     * @return whether this is a valid donate num
     */
    public boolean checkInput() {
        if (this.hostName == null) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("No room detected");
            errorAlert.setContentText("In order to donate, please enter a room first.");
            errorAlert.showAndWait();
            DonationInput.setText("");
            DonationInput.requestFocus();
            return false;
        } else if (this.userName.equals(this.hostName)) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Incorrect donation operation");
            errorAlert.setContentText("Please donate to others");
            errorAlert.showAndWait();
            DonationInput.setText("");
            DonationInput.requestFocus();
            return false;
        } else if (donateNum <= 0) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Donate Number");
            errorAlert.setContentText("Please enter a positive donate number.");
            errorAlert.showAndWait();
            this.alreadyInit = false;
            DonationInput.setText("");
            DonationInput.requestFocus();
            return false;
        } else if (donateNum > this.balance) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Donation");
            errorAlert.setContentText("Sorry, insufficient balance.");
            errorAlert.showAndWait();
            DonationInput.setText("");
            DonationInput.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Setting target room's online user list in the chatting page.
     * @param userList current user list for the room
     */
    public void setOnlineUserList(String userList) {
        userListBox.getChildren().clear();

        if (userList == null) return;
        //    user list panel
        GridPane gridPane = new GridPane();
        GridPane gridPane2 = new GridPane();

        TextValidator validator = new TextValidator();
        String messageEmoji = validator.replaceEmoji("");
        TextArea textArea = new TextArea(messageEmoji);

        TextArea userListArea = new TextArea(userList);
        double width2 = 180.0;
        userListArea.setPrefWidth(width2);

        int height2 = (userListArea.getText().split("\r\n|\r|\n").length * 17) + 14;
        userListArea.setMinHeight(height2);
        userListArea.setMaxHeight(height2);
        userListArea.setEditable(false);
        userListArea.getStyleClass().add("userListArea");

        gridPane2.add(userListArea, 2, 0);
        gridPane.add(gridPane2, 1, 0);
        userListBox.getChildren().add(gridPane);
    }

}