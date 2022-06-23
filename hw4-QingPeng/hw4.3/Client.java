import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private Client() {}

    public static void main(String[] args) throws UnknownHostException {
        //args give message contents and server hostname
        InetAddress aHost = null;
        int serverPort = 32009;
        if (args.length < 2) {
            LOGGER.info("Usage: java Client localhost:" + serverPort);
            aHost = InetAddress.getByName("localhost");
        } else {
            try {
                if (Character.isDigit(args[0].charAt(0))) {
                    aHost = InetAddress.getByAddress(args[0].getBytes());
                } else {
                    aHost = InetAddress.getByName(args[0]);
                }
                serverPort = Integer.valueOf(args[1]).intValue();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        try {
            Registry registry = LocateRegistry.getRegistry(aHost.getHostAddress(), serverPort);
            SorService sorService = (SorService) registry.lookup("sortService");
            BufferedReader reader = null;
            reader = new BufferedReader(
                    new InputStreamReader(System.in)
            );
            try {
                while (true) {
                    boolean prepared = true;
                    //input a string
                    System.out.println("Input a list of 10 integers you want to sort to the server:");
                    List<Integer> list = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        System.out.println("Input the " + (i + 1) + " number:");
                        String number = reader.readLine();
                        try {
                            list.add(Integer.valueOf(number));
                        } catch (NumberFormatException e) {
                            System.out.println("Please input an integer.");
                            prepared = false;
                            break;
                        }
                    }
                    if (prepared) {
                        List<Integer> sortedList = sorService.sort(list);
                        for (int num : sortedList) {
                            System.out.print(num + " ");
                        }
                        System.out.println();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
