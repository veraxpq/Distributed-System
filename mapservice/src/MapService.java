import java.util.HashMap;
import java.util.Map;

//This class handle the request from the client: save the key and value for the "put" request; retrieve the value of the
//given key for the "get" request; delete the key value pair for the "delete" request
public class MapService {
    Map<String, String> map = new HashMap<>();

    String map(String[] command, String host, int port) {
        String value = "";
        String operation = command[0].toLowerCase();
        if (operation.equals("put")) {
            if (command.length != 3) {
                Log.log("Received malformed request from " + host + ":" + port);
                value = "The request is not correct!";
            } else {
                map.put(command[1], command[2]);
            }
        } else if (operation.equals("get")) {
            if (command.length != 2) {
                Log.log("Received malformed request of length from " + host + ":" + port);
                value = "The request is not correct!";
            } else {
                if (map.containsKey(command[1])) {
                    value = map.get(command[1]);
                } else {
                    value = "No such key stored.";
                }
            }
        } else if (operation.equals("delete")) {
            if (command.length != 2) {
                Log.log("Received malformed request of length from " + host + ":" + port);
                value = "The request is not correct!";
            } else {
                map.remove(command[1]);
            }
        }
        return value;
    }
}
