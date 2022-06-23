package guis;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import server.Receiver;
import user.Main;
import user.Property;
import user.UserSocket;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This is the home controller for the client. It has the login functionality that gets the user
 * into the app.
 */
public class homeController {
    private final UserSocket userSocket;
    private String username;
    public static int clientObjPort;
    private String host = "localhost";
    private int serverPort = 50000;

    public TextField usernameInput;
    private static final Logger LOGGER = Logger.getLogger(Receiver.class.getName());

    /**
     * HomeController class constructor
     */
    public homeController() {
        // Desired socket port and host
        Property property = new Property();
        userSocket = new UserSocket(property.getProperty("host"), Integer.parseInt(property.getProperty("port")));
    }

    /**
     * Login function for home controller
     * @param e action
     * @throws IOException IOException
     */
    public void loginAction(ActionEvent e) throws IOException {
        this.username = this.usernameInput.getText();
        if (this.username.length() < 1) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("Username can't be empty. Please enter again.");
            errorAlert.showAndWait();
        } else {
            LOGGER.info("A new user login: "+ this.username);
            joinServer(e);
        }
    }

    /**
     * Join function for home controller
     * @param e action
     * @throws IOException IOException
     */
    private void joinServer(ActionEvent e) throws IOException {
        // Initiate chat screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/chatScreen.fxml"));
        Parent root = loader.load();
        chatController controller = loader.getController();
        controller.setUserSocket(userSocket, this.username);

        // Wait till connected
        boolean connected = userSocket.connectSocket(this.username, controller);
        if (connected) {
            // Open chat screen
            Scene child2 = new Scene(root, Main.stageWidth, Main.stageHeight);
            child2.getStylesheets().add(getClass().getResource("/resources/style.css").toExternalForm());
            Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
            window.setScene(child2);
            window.show();
        } else {
            // Show error popup
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Server error");
            errorAlert.setContentText("Unable to connect to server, please try again later.");
            errorAlert.showAndWait();
        }
    }
}
