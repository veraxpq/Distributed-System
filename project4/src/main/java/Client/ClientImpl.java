package Client;

import Server.Server;
import paxos.ProposalID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is the implementation of Client.
 */
public class ClientImpl implements Client {
    private static final Logger LOGGER = Logger.getLogger(ClientImpl.class.getName());
    private static int clientObjPort = 50000;
    private static String host = "localhost";
    private static int serverNum = 5;

    @Override
    public void getResponse(ProposalID proposalID, Object value) throws RemoteException {
        System.out.println(value);
    }

    /**
     * This is the main function of ClientImpl class, it run the service of the client, take a string as command, and
     * send it to a server to the the result.
     *
     * @param args the input port numbers, including 5 distinct por numbers of the servers.
     */
    public static void main(String[] args) {
        List<Integer> portList = new ArrayList<>();
        List<Server> serverList = new ArrayList<>();
        if (args.length != 5) {
            LOGGER.warning("Please input 5 port numbers of the servers");
            return;
        }
        try {
            for (int i = 0; i < args.length; i++) {
                portList.add(Integer.parseInt(args[i]));
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Please input 5 port numbers of the servers");
            return;
        }

        try {
            Client client = new ClientImpl();
            Client stub = (Client) UnicastRemoteObject.exportObject(client, clientObjPort);
            LOGGER.info("Client is running in port: " + clientObjPort);

            //bind the remote object with the registry
            Registry registry = LocateRegistry.createRegistry(clientObjPort);
            registry.rebind("client", stub);
            LOGGER.info("Client is registered remotely...");

            for (int i = 0; i < portList.size(); i++) {
                Registry serverRegistry = LocateRegistry.getRegistry(host, portList.get(i));
                Server server = (Server) serverRegistry.lookup("server" + portList.get(i));
                serverList.add(server);
            }

            BufferedReader reader = null;
            int cnt = 0;

            try {
                while (true) {
                    //input a string
                    System.out.println("Input the command in this format: PUT/GET/DELETE KEY VALUE");
                    reader = new BufferedReader(
                            new InputStreamReader(System.in)
                    );
                    String command = reader.readLine();
                    Server curServer = serverList.get(cnt++ % serverNum);
                    curServer.sendRequest(command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
