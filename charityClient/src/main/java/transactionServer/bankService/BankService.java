package transactionServer.bankService;

import org.json.simple.JSONArray;

import java.io.Serializable;
import java.util.List;

/**
 * This interface is to provide map service that can store key-value pair.
 */
public interface BankService extends Serializable {

    /**
     * This method takes a command as input, and execute the command.
     * put: record the key-value pair
     * get: return the value of the key stored in the system
     * delete: remove the key-value pair from the system
     *
     * @param command the command to operate the key-value pair
     * @return return the result of the execution.
     */
    JSONArray map(List<BankOperation> command);

    /**
     * This method roll back the last transaction.
     */
    void revert();

}