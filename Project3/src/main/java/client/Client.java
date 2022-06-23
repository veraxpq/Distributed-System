package client;

import util.Checker;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * this class represents a client that can send request to any one of the five servers. To specify the port of the servers,
 * you should input 5 ports as argument when run the program.
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * This method is the main method of the class. It takes arguments as the ports of five servers, send requests
     * according to the order of the servers periodically.
     *
     * @param args input arguments
     * @throws UnknownHostException when the client cannot find the open port.
     */
    public static void main(String[] args) throws UnknownHostException {
        //args give message contents and server hostname
        String host = "localhost";
        List<Integer> ports = new ArrayList<>();
        int count = 0;
        if (args.length != 5) {
            LOGGER.info("Please input 5 ports of servers, split them with a space.");
            return;
        }
        try {
            for (int i = 0; i < 5; i++) {
                ports.add(Integer.parseInt(args[i]));
            }
        } catch (NumberFormatException e) {
            LOGGER.info("Please input 5 ports of servers, split them with a space.");
            return;
        }

        try {
            BufferedReader reader = null;

            try {
                while (true) {
                    int port = ports.get((count++) % 5);
                    Socket socket = new Socket(host, port);
                    //input a string
                    System.out.println("Input the command in this format: PUT/GET/DELETE KEY VALUE");
                    reader = new BufferedReader(
                            new InputStreamReader(System.in)
                    );
                    String command = reader.readLine();
                    if (!Checker.invalidInput(command)) {
                        System.out.println("Invalid command");
                        continue;
                    }
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()));
                    writer.write(command);
                    writer.newLine();
                    writer.flush();
                    System.out.println("Send request to server " + port);
                    //get the result from the server
                    BufferedReader reader2 = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );
                    System.out.println(reader2.readLine());
                    //when done, just close the connection and exit
                    reader2.close();
                    writer.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
