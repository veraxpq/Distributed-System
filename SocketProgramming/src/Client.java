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
            System.out.println("Please input a string in maximum 80 characters");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in)
            );
            String text = reader.readLine();
            if (text.length() > 80 || text.length() == 0) {
                System.out.println("Please input a string in maximum 80 characters");
            } else {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(s1.getOutputStream()));
                writer.write(text);
                writer.newLine();
                writer.flush();
                //get the result from the server
                BufferedReader reader2 = new BufferedReader(
                        new InputStreamReader(s1.getInputStream())
                );
                System.out.println(reader2.readLine());
                //when done, just close the connection and exit
                reader2.close();
                writer.close();
            }
            reader.close();
            s1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
