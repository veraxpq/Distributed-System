import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Server implements SorService {
    private static final Logger LOGGER = Logger.getLogger(SorService.class.getName());
    private static Map<String, String> map = new ConcurrentHashMap<>();

    public Server() {}

    @Override
    public List<Integer> sort(List<Integer> list) throws RemoteException {
        List<Integer> copiedList = new ArrayList<>();
        for (int num : list) {
            copiedList.add(num);
        }
        Collections.sort(copiedList);
        return copiedList;
    }

    public static void main(String[] args) {
        int port = 32009;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Server server = new Server();
            SorService stub = (SorService) UnicastRemoteObject.exportObject(server, port);
            LOGGER.info("Server is running in port: " + port);

            //bind the remote object with the registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("sortService", stub);
            LOGGER.info("Server ready...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
