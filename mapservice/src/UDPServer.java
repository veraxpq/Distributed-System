import java.io.*;
import java.net.*;

public class UDPServer {
    public static void main(String[] args) throws IOException {
        MapService mapService = new MapService();
        DatagramSocket socket1 = null;
        int port = 32012;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server is running in port: " + port);
        socket1 = new DatagramSocket(port);

        try {
            while (true) {
                //register service
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket1.receive(request);
                if (request.getData() == null || request.getData().length == 0) {
                    continue;
                }
                //read the message from the client
                InetAddress address = request.getAddress();
                port = request.getPort();
                String line = new String(request.getData(), 0, request.getLength());
                Log.log("Received the request from client: " + address + ":" + port);
                Log.log("The request is: " + line);
                String[] command = line.split(" ");
                String value = mapService.map(command, address.getHostName(), port);
                if (value != null && value.length() != 0) {
                    byte[] send = value.getBytes();
                    DatagramPacket reply = new DatagramPacket(send, send.length, address, port);
                    socket1.send(reply);
                    Log.log("Reply: " + value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //close the connection
            socket1.close();
        }
    }
}
