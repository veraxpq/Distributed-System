package Server;

import paxos.Acceptor;
import paxos.Learner;
import paxos.Proposer;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This represents a Server which contains several roles, including proposer, learner and acceptor.
 */
public interface Server extends Serializable, Remote, Proposer, Learner, Acceptor {

    /**
     * This method send requests from the client to the server.
     *
     * @param command the command from the client.
     * @throws RemoteException
     */
    void sendRequest(String command) throws RemoteException;

}
