import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        String hostname = "localhost";
        int port = 32000;
        if (args.length != 2) {
            System.out.println("use the default setting.");
        } else {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }
        try {
            //open the connection to a server, at port 32001
            Socket s1 = new Socket(hostname, port);

            //input a string
            System.out.println("Choose one way to run the service: single thread or multi thread.");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in)
            );
            String text = reader.readLine();
            text.toLowerCase();
            if (text.indexOf("single") != -1 && text.indexOf("multi") != -1) {
                System.out.println("Choose one way to run the service: single thread or multi thread.");
            } else {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(s1.getOutputStream()));
                writer.write(text);
                writer.newLine();
                writer.flush();
//                //get the result from the server
//                BufferedReader reader2 = new BufferedReader(
//                        new InputStreamReader(s1.getInputStream())
//                );
//                System.out.println(reader2.readLine());
                //when done, just close the connection and exit
//                reader2.close();
                writer.close();
            }
            reader.close();
            s1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
