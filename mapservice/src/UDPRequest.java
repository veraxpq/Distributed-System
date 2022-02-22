import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

class UDPRequest {
    public void UDPRequest(String[] args, InetAddress aHost, int serverPort) throws IOException {
        DatagramSocket aSocket = null;
        BufferedReader reader = null;
        try {
            while (true) {
                //input the command
                Log.log("Input the command in this format: PUT/GET/DELETE KEY VALUE");
                reader = new BufferedReader(
                        new InputStreamReader(System.in)
                );
                String command = reader.readLine();
                if (Util.invalidInput(command)) {
                    Log.log("The input is invalid");
                } else {
                    byte[] m = command.getBytes();
                    //open the connection to the server
                    aSocket = new DatagramSocket();
                    //set timeout mechanism
                    aSocket.setSoTimeout(3000);
                    DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
                    try {
                        aSocket.send(request);
                    } catch (SocketTimeoutException e) {
                        Log.log("Timeout: " + e.getMessage());
                        continue;
                    }
                    if (command.toLowerCase().startsWith("get")) {
                        byte[] buffer = new byte[1000];
                        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                        try {
                            aSocket.receive(reply);
                        } catch (SocketTimeoutException e) {
                            Log.log("Timeout: " + e.getMessage());
                            continue;
                        }
                        Log.log("reply: " + new String(reply.getData(), 0, reply.getLength()));
                    }
                }
            }
        } catch (SocketException e) {
            Log.log("Socket: " + e.getMessage());
        } catch (IOException e) {
            Log.log("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }
}
