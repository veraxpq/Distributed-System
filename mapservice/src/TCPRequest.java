import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

class TCPRequest {
    public void TCPRequest(String hostname, int port) throws IOException {
        Socket s1 = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            while (true) {
                //input a string
                Log.log("Input the command in this format: PUT/GET/DELETE KEY VALUE");
                reader = new BufferedReader(
                        new InputStreamReader(System.in)
                );
                String command = reader.readLine();
                if (Util.invalidInput(command)) {
                    Log.log("The input is invalid");
                } else {
                    //open the connection to a server
                    try {
                        s1 = new Socket(hostname, port);
                        s1.setSoTimeout(3000);
                    } catch (SocketTimeoutException e) {
                        Log.log("Timeout: " + e.getMessage());
                        continue;
                    }
                    //set timeout mechanism
                    writer = new BufferedWriter(
                            new OutputStreamWriter(s1.getOutputStream()));
                    writer.write(command);
                    writer.newLine();
                    writer.flush();
                    if (command.toLowerCase().startsWith("get")) {
                        try {
                            DataInputStream in = new DataInputStream(new BufferedInputStream(s1.getInputStream()));
                            String result = in.readLine();
                            Log.log(result);
                        } catch (SocketTimeoutException e) {
                            Log.log("Timeout: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }
}
