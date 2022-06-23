package transactionServer.paxos;

import org.json.simple.JSONArray;
import transactionServer.Server.TransactionServer;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents a messenger that communicates between proposers, acceptors and learners.
 */
public interface Messenger extends Remote, Serializable {

    /**
     * This method registers a server in the messenger object, records the server object and UID into a map.
     *
     * @param UID    UID of the server.
     * @param server remote object of the server.
     * @throws RemoteException
     */
    void register(String UID, TransactionServer server) throws RemoteException;

    /**
     * This method send prepare messages from proposers to acceptors.
     *
     * @param proposalID the object of the proposal.
     * @throws RemoteException
     */
    void sendPrepare(ProposalID proposalID) throws RemoteException;

    /**
     * This method send promise from acceptors to proposers.
     *
     * @param proposerUID   the UID of the proposer.
     * @param proposalID    the object of the proposal.
     * @param previousID    the previous proposal accepted in the acceptor.
     * @param acceptedValue the previously accepted value of the acceptor.
     * @throws RemoteException
     */
    void sendPromise(String proposerUID, ProposalID proposalID, ProposalID previousID, Object acceptedValue) throws RemoteException;

    /**
     * This method sends accept messages from proposers to acceptors.
     *
     * @param acceptorUID   UID of the acceptor.
     * @param proposalID    the object of the proposal.
     * @param proposalValue the value of the proposal.
     * @throws RemoteException
     */
    void sendAccept(String acceptorUID, ProposalID proposalID, Object proposalValue) throws RemoteException;

    /**
     * This method send accepted messages from acceptors to learners.
     *
     * @param acceptorUID   UID of the acceptor.
     * @param proposalID    the object of the proposal.
     * @param acceptedValue the accepted value of the proposal.
     * @throws RemoteException
     */
    void sendAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue) throws RemoteException;

    /**
     * This method send the result from the learner to the client.
     *
     * @param proposalID the object of the proposal.
     * @param response   the response of the proposal.
     * @param client     the remote object of the client.
     * @throws RemoteException
     */
    void onResolution(ProposalID proposalID, JSONArray response) throws RemoteException;

    /**
     * This method send the client port to all the learners
     */
    void sendClientPort(int port) throws RemoteException;
}
