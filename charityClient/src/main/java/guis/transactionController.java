package guis;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import transactionServer.Client.TransactionClient;
import transactionServer.Server.TransactionServer;
import transactionServer.Server.TransactionServerImpl;
import transactionServer.bankService.BankOperation;
import transactionServer.bankService.Operation;
import transactionServer.paxos.ProposalID;
import user.UserSocket;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is the transaction controller to handles the transaction of
 * the user to the room host. It will send the transaction information
 * to the transaction client to go through the Paxos process in the
 * transaction server and sends back the result.
 */
public class transactionController implements TransactionClient {
    public long balance;
    public static String username = null;
    public String hostName = null;
    public int donateNum = Integer.MAX_VALUE;
    public static String host = "localhost";
    public static int serverNum = 5;
    private static int cnt = 0;
    public boolean isSuccess = Boolean.parseBoolean(null);

    private static List<TransactionServer> serverList = null;
    private static TransactionClient stub = null;
    protected boolean error = false;
    private static int clientObjPort = 60000;
    private static final Logger LOGGER = Logger.getLogger(TransactionServerImpl.class.getName());

    public transactionController() {

    }


    /**
     * This function set up the basic transaction information to
     * the transaction controller
     * @param balance the current balance of the user
     * @param username the user who is going to donate money
     * @param hostName the host name that will accept the donation
     * @param donateNum the number of the donation
     */
    public void setup (long balance, String username,
                      String hostName, int donateNum) {
        this.balance = balance;
        this.username = username;
        this.hostName = hostName;
        this.donateNum = donateNum;
    }

    /**
     * This function will initiate the basic settings of transaction client
     */
    public void initiate() {
        // add the 5 server ports to list
        List<Integer> portList = new ArrayList<>(
                Arrays.asList(50010, 50011, 50012, 50013, 50014));
        serverList = new ArrayList<>();

        try {
            for (int i = 0; i < portList.size(); i++) {
                Registry serverRegistry = LocateRegistry.getRegistry(host, portList.get(i));
                TransactionServer server = (TransactionServer) serverRegistry.lookup("server" + portList.get(i));
                serverList.add(server);
            }

            TransactionServer curServer = serverList.get(cnt % serverNum);
            clientObjPort = curServer.getClientPort();
            transactionController controller = new transactionController();
            stub = (TransactionClient) UnicastRemoteObject.exportObject(controller, clientObjPort);
            LOGGER.info("Client is running in port: " + clientObjPort);

            //bind the remote object with the registry
            Registry registry = LocateRegistry.createRegistry(clientObjPort);
            registry.rebind("client" + clientObjPort, stub);
            LOGGER.info("Client is registered remotely...");

        } catch (Exception e) {
            LOGGER.warning("Exception: " + e.getMessage());
        }
    }


    /**
     * This function sends the transaction information from transaction client
     * to transaction and send the paxos result back
     */
    public void prepareTrans() {
        isSuccess = false;

        List<String> commands = Arrays.asList(new String[]{
                "Withdraw" + " " + this.username + " " + this.donateNum,
                "Deposit" + " " + this.hostName + " " + this.donateNum
        });

        try {
            List<BankOperation> operation = createBankOperation(commands);
            TransactionServer curServer = serverList.get(cnt++ % serverNum);
            curServer.sendRequest(operation, clientObjPort);
            this.isSuccess = true;
        } catch (IllegalArgumentException | RemoteException e) {
            isSuccess = false;
            error = true;
            sendErrorMessage();
        }
    }


    /**
     * This function receives response from transaction server
     * @param proposalID the proposal object that the transaction info is saved
     * @param value      the result string sent back to client
     * @throws RemoteException if any error
     */
    @Override
    public void getResponse(ProposalID proposalID, Object value) throws RemoteException {
        if (!(value instanceof JSONArray)) {
            sendErrorMessage();
            return;
        }
        JSONArray responses = (JSONArray) value;
        for (int i = 0; i < responses.size(); i++) {
            JSONObject response = (JSONObject) responses.get(i);
            if (response.get("error") != null) {
                sendErrorMessage();
                error = true;
                return;
            } else if (response.get("account").equals(username)) {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        long balance = Long.valueOf(String.valueOf(response.get("balance")));
                        synchronized (chatController.newMessages) {
                            chatController.newMessages.push("/trans " + username + " " + balance);
                            chatController.messageWaiting = true;

                        }

                        Alert errorAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        errorAlert.setHeaderText("Donation Succeed!");
                        errorAlert.setContentText("Thanks for your donation.");
                        errorAlert.showAndWait();
                    }
                });
                return;
            }
        }
        return;
    }

    /**
     * This function sends the error message and display alert to users
     */
    private void sendErrorMessage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Donation Failed");
                errorAlert.setContentText("Please repeat the operation.");
                errorAlert.showAndWait();
            }
        });
    }

    /**
     * This function set up the bank service and sends the transaction info to server
     * @param commands the commands include the transaction info
     * @return the list of transaction operation
     * @throws IllegalArgumentException if any error
     */
    public static List<BankOperation> createBankOperation(List<String> commands) throws IllegalArgumentException {
        List<BankOperation> bankOperations = new ArrayList<>();
        for (String command : commands) {
            String[] words = command.split(" ");
            if (words.length > 3 || words.length < 2) {
                LOGGER.warning("Format error: Input the command in this format: Read/Deposit/Withdraw Account (Amount)");
                throw new IllegalArgumentException();
            }
            BankOperation operation = null;
            String type = words[0];
            String account = words[1];
            if (words.length == 2 && type.toLowerCase().equals("read")) {
                operation = new BankOperation(Operation.READ, account);
            } else if (words.length == 3) {
                if (type.toLowerCase().equals("deposit")) {
                    try {
                        long value = Long.valueOf(words[2]);
                        operation = new BankOperation(Operation.DEPOSIT, account, value);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Format error: Input the command in this format: Read/Deposit/Withdraw Account (Amount)");
                        throw new IllegalArgumentException();
                    }
                } else if (type.toLowerCase().equals("withdraw")) {
                    try {
                        long value = Long.valueOf(words[2]);
                        operation = new BankOperation(Operation.WITHDRAW, account, value);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Format error: Input the command in this format: Read/Deposit/Withdraw Account (Amount)");
                        throw new IllegalArgumentException();
                    }
                }
            }
            if (operation == null) {
                LOGGER.warning("Format error: Input the command in this format: Read/Deposit/Withdraw Account (Amount)");
                throw new IllegalArgumentException();
            } else {
                bankOperations.add(operation);
            }
        }
        return bankOperations;
    }

}