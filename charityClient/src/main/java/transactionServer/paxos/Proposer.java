package transactionServer.paxos;


import transactionServer.Client.TransactionClient;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents a proposer.
 */
public interface Proposer extends Remote, Serializable {

    /**
     * This method initializes the proposal with the given value and the client.
     *
     * @param value  the value of the proposal.
     * @param client the remote object of the client.
     * @throws RemoteException
     */
    void setProposal(Object value, TransactionClient client) throws RemoteException;

    /**
     * This method prepare a proposal for the proposer, and sends it to all the acceptors.
     *
     * @throws RemoteException
     */
    void prepare() throws RemoteException;

    /**
     * This method receives promises from acceptors.
     *
     * @param fromUID           UID of the acceptors.
     * @param proposalID        object of the proposal.
     * @param prevAcceptedID    object of the previously accepted proposal.
     * @param prevAcceptedValue value of the previously accepted proposal.
     * @throws RemoteException
     */
    void receivePromise(String fromUID, ProposalID proposalID, ProposalID prevAcceptedID, Object prevAcceptedValue) throws RemoteException;

}
