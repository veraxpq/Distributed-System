public class Util {
    public static boolean invalidInput(String command) {
        if (command == null || command.length() < 2 || command.length() > 3) {
            return false;
        }
        String[] words = command.split(" ");
        if ((words[0].toLowerCase().equals("put") && words.length == 3)
                || ((words[0].toLowerCase().equals("get") || words[0].toLowerCase().equals("delete")) && words.length == 2)) {
            return true;
        }
        return false;
    }

}
