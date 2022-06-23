package transactionServer.paxos;

import org.json.simple.JSONArray;
import transactionServer.Server.TransactionServer;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This is the implementation of the messenger interface.
 */
public class MessengerImpl implements Messenger {

    private static final Logger LOGGER = Logger.getLogger(MessengerImpl.class.getName());

    private Map<String, TransactionServer> serverMap;
    private static final int messengerPort = 50050;
    private static String host = "localhost";

    /**
     * This is the constructor of the MessengerImpl, initializes the attributes.
     */
    public MessengerImpl() {
        serverMap = new HashMap<String, TransactionServer>();
    }

    @Override
    public void register(String serverUID, TransactionServer server) throws RemoteException {
        serverMap.put(serverUID, server);
    }

    @Override
    public void sendPrepare(ProposalID proposalID) throws RemoteException {
        for (String acceptorUID : serverMap.keySet()) {
            serverMap.get(acceptorUID).receivePrepare(proposalID, acceptorUID);
        }
    }

    @Override
    public void sendPromise(String acceptorUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue) throws RemoteException {
        for (String proposerUID : serverMap.keySet()) {
            serverMap.get(proposerUID).receivePromise(acceptorUID, proposalID, previousID, acceptedValue);
        }
    }

    @Override
    public void sendAccept(String acceptorUID, ProposalID proposalID, Object proposalValue) throws RemoteException {
        for (String everyAcceptorUID : serverMap.keySet()) {
            serverMap.get(everyAcceptorUID).receiveAcceptRequest(everyAcceptorUID, proposalID, proposalValue);
        }
    }

    @Override
    public void sendAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue) throws RemoteException {
        boolean accept = false;
        for (String learnerUID : serverMap.keySet()) {
            if (serverMap.get(learnerUID).receiveAccepted(acceptorUID, proposalID, acceptedValue)) {
                accept = true;
            }
        }
        if (!accept) {
            for (String learnerUID : serverMap.keySet()) {
                serverMap.get(learnerUID).revert();
            }
        }
    }

    @Override
    public void onResolution(ProposalID proposalID, JSONArray response) throws RemoteException {
        proposalID.getClient().getResponse(proposalID, response);
    }

    @Override
    public void sendClientPort(int port) throws RemoteException {
        for (String learnerUID : serverMap.keySet()) {
            serverMap.get(learnerUID).receiveClientPort(port);
        }
    }

    /**
     * This is the main methods of the MessengerImpl class, registers a service into a port.
     *
     * @param args input arguments
     */
    public static void main(String[] args) {
        try {
            Messenger messenger = new MessengerImpl();
            Messenger stub = (Messenger) UnicastRemoteObject.exportObject(messenger, messengerPort);
            LOGGER.info("Messenger is running in port: " + messengerPort);

            //bind the remote object with the registry
            Registry registry = LocateRegistry.createRegistry(messengerPort);
            registry.rebind("messenger", stub);
            LOGGER.info("Messenger is ready...");
        } catch (AccessException e) {
            LOGGER.warning("access exception: " + e.getMessage());
        } catch (RemoteException e) {
            LOGGER.warning("remote exception: " + e.getMessage());
        }
    }
}
