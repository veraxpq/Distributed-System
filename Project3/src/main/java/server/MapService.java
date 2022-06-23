package server;

/**
 * This interface is to provide map service that can store key-value pair.
 */
public interface MapService {

    /**
     * This method takes a command as input, and execute the command.
     * put: record the key-value pair
     * get: return the value of the key stored in the system
     * delete: remove the key-value pair from the system
     *
     * @param command the command to operate the key-value pair
     * @return return the result of the execution.
     */
    String map(String command);

    /**
     * This method returns if the command is a get request.
     *
     * @param command the command to operate the key-value pair
     * @return true if it is a get request; false if it is not a get request.
     */
    boolean isGetRequest(String command);

    /**
     * This method returns the command that can roll back the command executed before.
     *
     * @return return a command that can roll back the command executed before.
     */
    String originalKVPair();

}