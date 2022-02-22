import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class StringReverseServer {
    public static void main(String[] args) throws IOException {
        int port = 32000;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server is running");
        //register service on port 32000
        ServerSocket socket = new ServerSocket(port);
        //wait and accept a connection
        Socket socket1 = socket.accept();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
            //read the message from the client
            String line = reader.readLine();
            StringBuilder sb = new StringBuilder();
            if (line.length() > 80) {
                sb.append("please input a string with the length between 0 to 80 characters.");
            } else {
                //reverse the string, and switch the upper case and lower case
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (Character.isLowerCase(c)) {
                        sb.insert(0, Character.toUpperCase(c));
                    } else if (Character.isUpperCase(c)) {
                        sb.insert(0, Character.toLowerCase(c));
                    } else {
                        sb.insert(0, c);
                    }
                }
            }
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket1.getOutputStream())
        );
            writer.write(sb.toString());
            writer.newLine();
            writer.flush();
            //close the stream and the connection
            reader.close();
            writer.close();
            socket1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
