package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents a participant that managed by a coordinator.
 */
public interface Participant extends Remote {

    /**
     * This method executes the command, and return the result whether it operates successfully.
     *
     * @param command the input command to operate the key-value pair.
     * @return return the result of the execution.
     * @throws RemoteException
     */
    String vote(String command) throws RemoteException;

    /**
     * this method execute the command to roll back to the status of operation.
     *
     * @param command the input command to operate the key-value pair in order to roll back.
     * @throws RemoteException
     */
    void abort(String command) throws RemoteException;
}
