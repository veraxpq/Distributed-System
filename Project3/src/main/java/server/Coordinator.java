package server;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This interface represents a coordinator to sync between servers.
 */
public interface Coordinator extends Remote, Serializable {

    /**
     * This method record the server that calls this method into a map.
     *
     * @param host hostname of the server
     * @param port port number of the server
     * @throws RemoteException
     * @throws IOException
     * @throws NotBoundException
     */
    void connect(String host, int port) throws RemoteException, IOException, NotBoundException;

    /**
     * This method is to start committing between all the servers. If any of them fails, then all the servers should
     * abort.
     *
     * @param info list contains the information of the server and the requests.
     * @return true if all servers commit; false if any of the servers fail.
     * @throws RemoteException
     */
    boolean startCommit(List<String> info) throws RemoteException;
}
