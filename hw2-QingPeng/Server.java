import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
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
            line = line.toLowerCase();
            if (line.indexOf("single") != -1) {
                SingleThread st = new SingleThread();
                st.run();
            } else if (line.indexOf("multi") != -1) {
                Thread mt = new Thread(new MultiThread());
                mt.start();

            }
            //close the stream and the connection
            reader.close();
            socket1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static class SingleThread {
        public SingleThread() {

        }
        public void run() {
            System.out.println("Run the service in single threading");
        }
    }
    static class MultiThread implements Runnable {
        public MultiThread() {

        }

        @Override
        public void run() {
            System.out.println("Run the service in multi threading");
        }
    }
}
