//This class is responsible add timestamp and print the log into the console.
public class Log {

    public static void log(String log) {
        System.out.println(getTime() + ": " + log);
    }

    private static String getTime() {
        return "" + System.currentTimeMillis();
    }
}
