package transactionServer.paxos;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents a learner, which get value from acceptors, and send the result to the client when there are
 * more than half proposals received.
 */
public interface Learner extends Remote, Serializable {

    /**
     * This method returns true when the final value is not null, and false otherwise.
     *
     * @return true when there is value for the final value, false otherwise.
     * @throws RemoteException
     */
    boolean isComplete() throws RemoteException;

    /**
     * This method receives accepted messages from acceptors, and send the result to the client when there are more than
     * half accepts.
     *
     * @param acceptorUID   the UID of the acceptor.
     * @param proposalID    the proposal object.
     * @param acceptedValue the accepted value of the proposal.
     * @throws RemoteException
     */
    boolean receiveAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue) throws RemoteException;

    /**
     * This method returns the final value of the proposal.
     *
     * @return the final value of the proposal.
     * @throws RemoteException
     */
    Object getFinalValue() throws RemoteException;

    /**
     * This method returns the final proposal object.
     *
     * @return returns the final proposal object.
     * @throws RemoteException
     */
    ProposalID getFinalProposalID() throws RemoteException;

    /**
     * This method revert the operations of a transaction.
     *
     * @throws RemoteException
     */
    void revert() throws RemoteException;

    /**
     * This method get a unique port number for the client.
     *
     * @return a unique port number for the client.
     */
    int getClientPort() throws RemoteException;

    /**
     * This method receives port from messenger, and record it if it is not recorded.
     *
     * @param port the given port number
     */
    void receiveClientPort(int port) throws RemoteException;
}
