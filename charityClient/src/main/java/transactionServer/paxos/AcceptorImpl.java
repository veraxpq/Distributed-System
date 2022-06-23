package transactionServer.paxos;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * This class represents the implementation of the Acceptor.
 */
public class AcceptorImpl implements Acceptor {
    private static final Logger LOGGER = Logger.getLogger(AcceptorImpl.class.getName());

    protected Messenger messenger;
    protected ProposalID promisedID;
    protected ProposalID acceptedID;
    protected Object acceptedValue;
    private static String host = "localhost";
    private static final int messengerPort = 50050;

    /**
     * This is the constructor of the AcceptorImpl class, it initializes the attributes.
     */
    public AcceptorImpl() {
        try {
            Registry registry = LocateRegistry.getRegistry(host, messengerPort);
            this.messenger = (Messenger) registry.lookup("messenger");
        } catch (AccessException e) {
            LOGGER.warning("access exception: " + e.getMessage());
        } catch (NotBoundException e) {
            LOGGER.warning("not bound exception: " + e.getMessage());
        } catch (RemoteException e) {
            LOGGER.warning("remote exception: " + e.getMessage());
        }
    }

    @Override
    public void receivePrepare(ProposalID proposalID, String acceptorUID) throws RemoteException {
        // duplicate message
        if (this.promisedID != null && proposalID.equals(promisedID)) {
            messenger.sendPromise(acceptorUID, proposalID, acceptedID, acceptedValue);
        } else if (this.promisedID == null || proposalID.isGreaterThan(promisedID)) {
            promisedID = proposalID;
            messenger.sendPromise(acceptorUID, proposalID, acceptedID, acceptedValue);
        }
    }

    @Override
    public void receiveAcceptRequest(String acceptorUID, ProposalID proposalID, Object value) throws RemoteException {
        if (promisedID == null || proposalID.isGreaterThan(promisedID) || proposalID.equals(promisedID)) {
            promisedID = proposalID;
            acceptedID = proposalID;
            acceptedValue = value;
            messenger.sendAccepted(acceptorUID, proposalID, acceptedValue);
        }
    }

}
