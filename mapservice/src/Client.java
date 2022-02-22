import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        //args give message contents and server hostname
        if (args.length < 3) {
            Log.log("Usage: java UDPClient localhost:32001");
            System.exit(1);
        }
        try {
            InetAddress aHost = null;
            if (Character.isDigit(args[0].charAt(0))) {
                aHost = InetAddress.getByAddress(args[0].getBytes());
            } else {
                aHost = InetAddress.getByName(args[0]);
            }
            int serverPort = Integer.valueOf(args[1]).intValue();
            if (args[2].toUpperCase().contains("UDP")) {
                UDPRequest udpRequest = new UDPRequest();
                udpRequest.UDPRequest(args, aHost, serverPort);
            } else if (args[2].toUpperCase().contains("TCP")) {
                TCPRequest tcpRequest = new TCPRequest();
                tcpRequest.TCPRequest(aHost.getHostName(), serverPort);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
