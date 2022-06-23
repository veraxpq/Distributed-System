package Server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class is the implementation of MapService interface.
 */
public class MapServiceImpl implements MapService {
    private static final Logger LOGGER = Logger.getLogger(MapServiceImpl.class.getName());
    private static Map<String, String> map = new ConcurrentHashMap<>();
    String command = null;
    String[] words = null;

    @Override
    public String map(String command) {
        command.trim();
        String[] words = command.split(" ");
        if (words[0].toLowerCase().equals("put")) {
            return put(words[1], words[2]);
        } else if (words[0].toLowerCase().equals("get")) {
            return get(words[1]);
        } else if (words[0].toLowerCase().equals("delete")) {
            return delete(words[1]);
        }
        throw new IllegalArgumentException("The input is invalid");
    }

    @Override
    public boolean isGetRequest(String command) {
        this.command = command;
        this.words = command.split(" ");
        return words[0].toLowerCase().equals("get");
    }

    @Override
    public String originalKVPair() {
        String key = words[1];
        if (map.containsKey(key)) {
            String value = map.get(key);
            return "put " + key + " " + value;
        } else {
            return "delete " + key;
        }
    }

    private String put(String key, String value) {
        try {
            LOGGER.info("put: " + key + ", " + value);
            map.put(key, value);
        } catch (Exception e) {
            LOGGER.warning("Fail to put " + key + ", " + value);
            return "Fail to put the key-value pair";
        }
        return "Success! You just put the key-value pair: " + key + ", " + value;
    }

    private String get(String key) {
        if (map.containsKey(key)) {
            LOGGER.info("get: " + key + ", " + map.get(key));
            return "Success! Value: " + map.get(key);
        } else {
            LOGGER.warning("Fail to get " + key);
            return "Failure. There is no such key.";
        }
    }

    private String delete(String key) {
        if (map.containsKey(key)) {
            map.remove(key);
            LOGGER.info("delete the key: " + key);
            return "Success! You just remove the key: " + key;
        } else {
            LOGGER.warning("Fail to delete key: " + key);
            return "Failure! There is no such key.";
        }
    }
}
