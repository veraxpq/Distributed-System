package transactionServer.paxos;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the interface of Acceptor. The acceptor waits for the proposal sent from the proposer, and there is a chance
 * of failure in the acceptor.
 */
public interface Acceptor extends Remote, Serializable {

    /**
     * This method receive prepare message from the proposer, and call the messenger to send back message to the proposer
     * there is a chance to fail.
     *
     * @param proposalID  the object of a proposal, including the UID of the proposal, number of the id, the command and
     *                    so on.
     * @param acceptorUID the UID of the server of the acceptor.
     * @throws RemoteException
     */
    void receivePrepare(ProposalID proposalID, String acceptorUID) throws RemoteException;

    /**
     * This method receives accept request from proposers, and send the proposal to the learner.
     *
     * @param acceptorUID the UID of the server of the acceptor.
     * @param proposalID  the proposal object.
     * @param value       the command string of the proposal.
     * @throws RemoteException
     */
    void receiveAcceptRequest(String acceptorUID, ProposalID proposalID, Object value) throws RemoteException;
}
