package server;

import util.Log;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class is the implementation of the interface Coordinator.
 */
public class CoordinatorService implements Coordinator {
    private static final Logger LOGGER = Logger.getLogger(CoordinatorService.class.getName());
    private static Map<String, String> map = new ConcurrentHashMap<>();

    private Map<Integer, Participant> connections;
    private static int port = 30000;
    private Log log;

    volatile int voteCounter;

    /**
     * Constructor of the CoordinatorService class.
     */
    public CoordinatorService() {
        connections = new HashMap<>();
        log = new Log();
    }

    /**
     * Main method of the CoordinatorService class, run the coordinator in a given port.
     *
     * @param args input argument to specify the port number.
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                LOGGER.warning("Please input a port number");
                return;
            }
        }
        try {
            Coordinator coordinator = new CoordinatorService();
            Coordinator stub = (Coordinator) UnicastRemoteObject.exportObject(coordinator, port);
            LOGGER.info("Coordinator is running in port: " + port);

            //bind the remote object with the registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("coordinator", stub);
            LOGGER.info("Coordinator service is ready...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String host, int remoteOPort) throws IOException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host, remoteOPort);
        Participant participant = (Participant) registry.lookup("server" + remoteOPort);
        this.connections.put(remoteOPort, participant);
        for (Map.Entry entry : connections.entrySet()) {
            System.out.println(entry.getKey());
        }
    }

    @Override
    public boolean startCommit(List<String> info) {
        voteCounter = 0;
        LOGGER.info("Start committing, the request is from: " + info.get(0));
        String original = info.get(1);
        String command = info.get(2);
        try {
            for (Participant p : connections.values()) {
                String res = p.vote(command);
                if (res.equals(Log.SELF_COMMIT)) {
                    voteCounter++;
                } else {
                    abort(original);
                    return false;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    private synchronized void abort(String command) {
        for (Participant p : connections.values()) {
            try {
                p.abort(command);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            LOGGER.info("self abort the request: " + command + "by: " + p.toString());
        }
    }
}
