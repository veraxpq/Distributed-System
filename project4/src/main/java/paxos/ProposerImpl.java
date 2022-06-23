package paxos;

import Client.Client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * This class is the implementation of the Proposer interface.
 */
public class ProposerImpl implements Proposer {

    private static final Logger LOGGER = Logger.getLogger(ProposerImpl.class.getName());

    protected Messenger messenger;
    protected String proposerUID;
    protected final int quorumSize;
    protected ProposalID proposalID;
    protected Object proposedValue = null;
    protected ProposalID lastAcceptedID = null;
    protected HashSet<String> promisesReceived = new HashSet<>();
    protected Client client = null;
    private static String host = "localhost";
    private static final int messengerPort = 50050;

    /**
     * This is the constructor of the proposerImpl class, initializes the attributes.
     *
     * @param proposerUID UID of the proposer.
     * @param quorumSize half size of the total proposers.
     */
    public ProposerImpl(String proposerUID, int quorumSize) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, messengerPort);
            this.messenger = (Messenger) registry.lookup("messenger");
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        this.proposerUID = proposerUID;
        this.quorumSize = quorumSize;
        this.proposalID = new ProposalID(0, proposerUID);
    }

    @Override
    public void setProposal(Object value, Client client) {
        this.client = client;
        if (proposedValue == null) {
            proposedValue = value;
        }
        proposalID.setClient(client);
        proposalID.setValue((String) value);
    }

    @Override
    public void prepare() throws RemoteException {
        promisesReceived.clear();
        proposalID.incrementNumber();
        messenger.sendPrepare(proposalID);
    }

    @Override
    public void receivePromise(String acceptorUID, ProposalID proposalID, ProposalID acceptedID, Object acceptedValue) throws RemoteException {
        if (!proposalID.equals(this.proposalID)) {
            LOGGER.warning("received proposal id is not equal to recorded proposal id");
            return;
        }
        if (promisesReceived.contains(acceptorUID)) {
            LOGGER.warning("Same acceptor promise received");
            return;
        }
        promisesReceived.add(acceptorUID);
        if (lastAcceptedID == null || acceptedID.isGreaterThan(lastAcceptedID)) {
            lastAcceptedID = acceptedID;
            if (acceptedValue != null) {
                proposedValue = acceptedValue;
            }
        }
        if (promisesReceived.size() == quorumSize) {
            if (proposedValue != null) {
                System.out.println("server: " + acceptorUID + "send promise to proposers");
                messenger.sendAccept(acceptorUID, proposalID, proposedValue);
            }
        }
    }
}
