package server;

import util.Checker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class represents a server that provide key-value store service.
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static int coordinatorPort = 30000;
    private static int clientPort = 30001;
    private static int remoteOPort = 31001;
    private static String host = "localhost";
    private static Coordinator coordinator = null;
    private static int connectionPort;

    /**
     * This is the main method of the Server class, which run the service in a given port.
     *
     * @param args the input argument should contains the coordinator port number, the server port number and the
     *             remote server port number.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        MapService mapService = new MapServiceImpl();

        if (args.length != 3) {
            LOGGER.warning("Please input the coordinator port number, the server port number and a remote server " +
                    "port number as arguments.");
            return;
        }
        try {
            coordinatorPort = Integer.parseInt(args[0]);
            clientPort = Integer.parseInt(args[1]);
            remoteOPort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            LOGGER.warning("Please input the coordinator port number, the server port number and a remote server port number as arguments.");
            return;
        }
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            //connect to remote service coordinator
            Registry registry = LocateRegistry.getRegistry(host, coordinatorPort);
            coordinator = (Coordinator) registry.lookup("coordinator");

            //register a remote service
            Participant participant = new ParticipantService();
            Participant stub = (Participant) UnicastRemoteObject.exportObject(participant, remoteOPort);
            LOGGER.info("Server is running in port: " + remoteOPort);

            //bind the remote object with the registry
            Registry serverRegistry = LocateRegistry.createRegistry(remoteOPort);
            serverRegistry.rebind("server" + remoteOPort, stub);
        } catch (RemoteException | NotBoundException e) {
            LOGGER.warning("The port of coordinator is wrong.");
            return;
        }

        ServerSocket serverSocket = null;
        Socket socket1 = null;

        try {
            serverSocket = new ServerSocket(clientPort);

            coordinator.connect(host, remoteOPort);

            while (true) {
                //wait and accept a connection
                socket1 = serverSocket.accept();
                if (!socket1.isConnected()) {
                    continue;
                }
                reader = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                String line = reader.readLine();
                LOGGER.info("ACK: received request from client " + socket1.getInetAddress() + ":" + socket1.getPort());
                LOGGER.info("the request is: " + line);
                //read the message from the client
                StringBuilder sb = new StringBuilder();
                if (!Checker.invalidInput(line)) {
                    sb.append("please input put/get/delete to operate a key-value pair.");
                } else {
                    if (mapService.isGetRequest(line)) {
                        sb.append(mapService.map(line));
                    } else {
                        String original = mapService.originalKVPair();
                        List<String> info = new ArrayList<>();
                        info.add(String.valueOf(clientPort));
                        info.add(original);
                        info.add(line);
                        if (coordinator.startCommit(info)) {
                            sb.append(mapService.map(line));
                        } else {
                            mapService.map(original);
                            sb.append("Operation failed");
                        }
                    }
                    writer = new BufferedWriter(
                            new OutputStreamWriter(socket1.getOutputStream())
                    );
                    writer.write(sb.toString());
                    writer.newLine();
                    writer.flush();
                }
            }
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (socket1 != null) {
                socket1.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }
}