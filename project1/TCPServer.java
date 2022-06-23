import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {
    public static void main(String[] args) throws IOException {
        MapService mapService = new MapService();
        int port = 32000;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server is running in port: " + port);
        //register service
        ServerSocket socket = new ServerSocket(port);
        //wait and accept a connection
        Socket socket1 = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            while (true) {
                socket1 = socket.accept();
                if (!socket1.isConnected()) {
                    continue;
                }
                reader = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                //read the message from the client
                InetAddress address = socket1.getInetAddress();
                String line = reader.readLine();
                Log.log("Received the request from client: " + address.getHostAddress() + ":" + socket1.getPort());
                Log.log("The request is: " + line);

                String[] command = line.split(" ");
                String value = mapService.map(command, address.getHostAddress(), socket1.getPort());
                if (value != null && value.length() != 0) {
                    writer = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
                    writer.write(value);
                    writer.newLine();
                    writer.flush();
                    Log.log("Reply: " + value);
                }
            }
            //close the stream and the connection
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.close();
            writer.close();
            socket1.close();
        }
    }
}
