package Server;

import Client.Client;
import paxos.*;

import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * This is the implementation of the server interface.
 */
public class ServerImpl implements Server {
    private static final Logger LOGGER = Logger.getLogger(ServerImpl.class.getName());

    private static Proposer proposer;
    private static Acceptor acceptor;
    private static Learner learner;
    private static String UID = "";
    private static int clientObjPort = 50000;
    private static String host = "localhost";
    private static int port;
    private static final int messengerPort = 50050;
    private static MapService mapService = null;

    /**
     * This is the constructor of the serverImpl class, initializes a proposer, an acceptor and a learner, and publish
     * the service into a port.
     *
     * @param args the given arguments.
     * @throws UnknownHostException
     */
    public static void main(String[] args) throws UnknownHostException {
        LOGGER.info("Server is running.");
        if (args.length != 1) {
            LOGGER.warning("Please input a unique port number to export the server.");
            return;
        }
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            LOGGER.warning("Please input a unique port number to export the server.");
            return;
        }
        UID = String.valueOf(port);
        mapService = new MapServiceImpl();

        try {
            Registry registry = LocateRegistry.getRegistry(host, messengerPort);
            Messenger messenger = (Messenger) registry.lookup("messenger");
            proposer = new ProposerImpl(UID, 3);
            acceptor = new AcceptorImpl();
            learner = new LearnerImpl(3, mapService, UID);

            Server server = new ServerImpl();
            Server stub = (Server) UnicastRemoteObject.exportObject(server, port);
            LOGGER.info("Server is running in port: " + port);
            messenger.register(UID, stub);

            //bind the remote object with the registry
            Registry serverRegistry = LocateRegistry.createRegistry(port);
            serverRegistry.rebind("server" + port, stub);
            LOGGER.info("Server ready...");
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendRequest(String command) throws RemoteException {
        try {
//            connect to remote service coordinator
            System.out.println("command: " + command);
            Registry clientRegistry = LocateRegistry.getRegistry(host, clientObjPort);
            Client client = (Client) clientRegistry.lookup("client");

            proposer.setProposal(command, client);
            proposer.prepare();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    //acceptor
    @Override
    public void receivePrepare(ProposalID proposalID, String acceptorUID) throws RemoteException {
        acceptor.receivePrepare(proposalID, acceptorUID);
    }

    @Override
    public void receiveAcceptRequest(String acceptorUID, ProposalID proposalID, Object value) throws RemoteException {
        acceptor.receiveAcceptRequest(acceptorUID, proposalID, value);
    }

    //learner
    @Override
    public boolean isComplete() throws RemoteException {
        return learner.isComplete();
    }

    @Override
    public void receiveAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue, Client client) throws RemoteException {
        learner.receiveAccepted(acceptorUID, proposalID, acceptedValue, client);
    }

    @Override
    public Object getFinalValue() throws RemoteException {
        return learner.getFinalValue();
    }

    @Override
    public ProposalID getFinalProposalID() throws RemoteException {
        return learner.getFinalProposalID();
    }

    //proposer
    @Override
    public void setProposal(Object value, Client client) throws RemoteException {
        proposer.setProposal(value, client);
    }

    @Override
    public void prepare() throws RemoteException {
        proposer.prepare();
    }

    @Override
    public void receivePromise(String fromUID, ProposalID proposalID, ProposalID prevAcceptedID, Object prevAcceptedValue) throws RemoteException {
        proposer.receivePromise(fromUID, proposalID, prevAcceptedID, prevAcceptedValue);
    }

}
