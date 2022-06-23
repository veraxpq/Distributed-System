package transactionServer.Client;

import transactionServer.paxos.ProposalID;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents a client, including a method getResponse() to be called and print the result.
 */
public interface TransactionClient extends Remote, Serializable {

    /**
     * This method print the result in the console.
     *
     * @param proposalID the proposal object
     * @param value      the result string
     * @throws RemoteException
     */
    void getResponse(ProposalID proposalID, Object value) throws RemoteException;
}
