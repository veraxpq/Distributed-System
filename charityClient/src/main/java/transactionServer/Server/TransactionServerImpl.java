package transactionServer.Server;

import transactionServer.Client.TransactionClient;
import transactionServer.bankService.BankOperation;
import transactionServer.bankService.BankService;
import transactionServer.bankService.BankServiceImpl;
import transactionServer.paxos.*;

import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is the implementation of the server interface.
 */
public class TransactionServerImpl implements TransactionServer {
    private static final Logger LOGGER = Logger.getLogger(TransactionServerImpl.class.getName());

    private static Proposer proposer;
    private static Acceptor acceptor;
    private static Learner learner;
    private static String UID = "";
    private static String host = "localhost";
    private static int port;
    private static final int messengerPort = 50050;
    private static BankService bankService = null;

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
        bankService = new BankServiceImpl();

        try {
            Registry registry = LocateRegistry.getRegistry(host, messengerPort);
            Messenger messenger = (Messenger) registry.lookup("messenger");
            proposer = new ProposerImpl(UID, 3);
            acceptor = new AcceptorImpl();
            learner = new LearnerImpl(3, bankService, UID);

            TransactionServer server = new TransactionServerImpl();
            TransactionServer stub = (TransactionServer) UnicastRemoteObject.exportObject(server, port);
            LOGGER.info("Server is running in port: " + port);
            messenger.register(UID, stub);

            //bind the remote object with the registry
            Registry serverRegistry = LocateRegistry.createRegistry(port);
            serverRegistry.rebind("server" + port, stub);
            LOGGER.info("Server ready...");
        } catch (AccessException e) {
            LOGGER.warning("access exception" + e.getMessage());
        } catch (RemoteException e) {
            LOGGER.warning("remote exception" + e.getMessage());
        } catch (NotBoundException e) {
            LOGGER.warning("not bound exception" + e.getMessage());
        }
    }

    @Override
    public void sendRequest(List<BankOperation> command, int clientObjPort) throws RemoteException {
        try {
//            connect to remote service coordinator
            LOGGER.info("command: " + command.toString());
            Registry clientRegistry = LocateRegistry.getRegistry(host, clientObjPort);
            TransactionClient client = (TransactionClient) clientRegistry.lookup("client" + clientObjPort);

            proposer.setProposal(command, client);

            proposer.prepare();
        } catch (NotBoundException e) {
            LOGGER.warning("not bound exception: " + e.getMessage());
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
    public boolean receiveAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue) throws RemoteException {
        return learner.receiveAccepted(acceptorUID, proposalID, acceptedValue);
    }

    @Override
    public Object getFinalValue() throws RemoteException {
        return learner.getFinalValue();
    }

    @Override
    public ProposalID getFinalProposalID() throws RemoteException {
        return learner.getFinalProposalID();
    }

    @Override
    public void revert() throws RemoteException {
        learner.revert();
    }

    //proposer
    @Override
    public void setProposal(Object value, TransactionClient client) throws RemoteException {
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

    @Override
    public int getClientPort() throws RemoteException {
        return learner.getClientPort();
    }

    @Override
    public void receiveClientPort(int port) throws RemoteException {
        learner.receiveClientPort(port);
    }
}
