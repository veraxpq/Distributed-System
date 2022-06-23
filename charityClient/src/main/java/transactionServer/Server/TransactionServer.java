package transactionServer.Server;


import transactionServer.bankService.BankOperation;
import transactionServer.paxos.Acceptor;
import transactionServer.paxos.Learner;
import transactionServer.paxos.Proposer;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This represents a Server which contains several roles, including proposer, learner and acceptor.
 */
public interface TransactionServer extends Serializable, Remote, Proposer, Learner, Acceptor {

    /**
     * This method send requests from the client to the server.
     *
     * @param command the command from the client.
     * @throws RemoteException
     */
    void sendRequest(List<BankOperation> command, int port) throws RemoteException;

}
