package util;

/**
 * This class is to check if the command is valid.
 */
public class Checker {
    public static boolean invalidInput(String command) {
        if (command == null || command.length() == 0) {
            return false;
        }
        String[] words = command.split(" ");
        if (words == null || words.length < 2 || words.length > 3) {
            return true;
        }
        if (invalidInput(words)) {
            return false;
        }
        return true;
    }

    private static boolean invalidInput(String[] words) {
        if (words == null || words.length < 2 || words.length > 3) {
            return true;
        }
        if ((words[0].toLowerCase().equals("put") && words.length == 3)
                || ((words[0].toLowerCase().equals("get") || words[0].toLowerCase().equals("delete")) && words.length == 2)) {
            return false;
        }
        return true;
    }
}
