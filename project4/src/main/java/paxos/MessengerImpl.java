package paxos;

import Client.Client;
import Server.Server;

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

    private Map<String, Server> serverMap;
    private Client client = null;
    private static final int messengerPort = 50050;
    private static String host = "localhost";

    /**
     * This is the constructor of the MessengerImpl, initializes the attributes.
     */
    public MessengerImpl() {
        serverMap = new HashMap<String, Server>();
    }

    @Override
    public void register(String serverUID, Server server) throws RemoteException {
        serverMap.put(serverUID, server);
    }

    @Override
    public void sendPrepare(ProposalID proposalID) throws RemoteException {
        client = proposalID.getClient();
        for (String acceptorUID : serverMap.keySet()) {
            serverMap.get(acceptorUID).receivePrepare(proposalID, acceptorUID);
        }
    }

    @Override
    public void sendPromise(String acceptorUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue) throws RemoteException {
        serverMap.get(proposalID.getUid()).receivePromise(acceptorUID, proposalID, previousID, acceptedValue);
    }

    @Override
    public void sendAccept(String acceptorUID, ProposalID proposalID, Object proposalValue) throws RemoteException {
        for (String everyAcceptorUID : serverMap.keySet()) {
            serverMap.get(everyAcceptorUID).receiveAcceptRequest(everyAcceptorUID, proposalID, proposalValue);
        }
    }

    @Override
    public void sendAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue) throws RemoteException {
        for (String learnerUID : serverMap.keySet()) {
            serverMap.get(learnerUID).receiveAccepted(acceptorUID, proposalID, acceptedValue, client);
        }
    }

    @Override
    public void onResolution(ProposalID proposalID, Object value, Client client) throws RemoteException {
        client.getResponse(proposalID, value);
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
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
