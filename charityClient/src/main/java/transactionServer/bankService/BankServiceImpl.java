package transactionServer.bankService;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class is the implementation of BankService interface.
 */
public class BankServiceImpl implements BankService {
    private static final Logger LOGGER = Logger.getLogger(BankServiceImpl.class.getName());
    private static Map<String, Long> map = new ConcurrentHashMap<>();
    private static List<BankOperation> reverts = new ArrayList<>();
    private long originalAmount = 1000;

    /**
     * This is the constructor of the BankServiceImpl class.
     */
    public BankServiceImpl() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public JSONArray map(List<BankOperation> commands) {
        recordLogs(commands);
        JSONArray array = new JSONArray();
        for (BankOperation command : commands) {
            if (command.getOperation() == Operation.READ) {
                array.add(read(command));
            } else if (command.getOperation() == Operation.DEPOSIT) {
                array.add(deposit(command));
            } else if (command.getOperation() == Operation.WITHDRAW) {
                array.add(withdraw(command));
            } else {
                JSONObject job = new JSONObject();
                job.put("error", "Please input a format of command");
                array.add(job);
            }
        }
        return array;
    }

    @Override
    public void revert() {
        map(reverts);
    }

    private void recordLogs(List<BankOperation> commands) {
        reverts.clear();
        for (BankOperation operation : commands) {
            if (operation.getOperation() == Operation.READ) {
                continue;
            }
            String account = operation.getAccount();
            if (!map.containsKey(account)) {
                map.put(account, originalAmount);
            }
            long amount = map.get(account);
            BankOperation revertOperation = null;
            if (operation.getOperation() == Operation.DEPOSIT) {
                revertOperation = new BankOperation(Operation.WITHDRAW, account, amount);
            } else if (operation.getOperation() == Operation.WITHDRAW) {
                revertOperation = new BankOperation(Operation.DEPOSIT, account, amount);
            }
            if (revertOperation != null) {
                reverts.add(revertOperation);
            }
        }
    }

    private JSONObject withdraw(BankOperation operation) {
        JSONObject job = new JSONObject();
        String account = operation.getAccount();
        if (!map.containsKey(account)) {
            map.put(account, originalAmount);
        }
        long amount = map.get(account);
        long updatedAmount = amount - operation.getValue();
        try {
            map.put(account, updatedAmount);
            job.put("account", account);
            job.put("balance", updatedAmount);
            LOGGER.info("withdraw into the account: " + operation.getAccount() + ", original balance: " + amount
                    + ", updated balance: " + updatedAmount);
        } catch (Exception e) {
            LOGGER.warning("Fail to read from " + operation.getAccount());
            job.put("error", "operation fails, please try again");
            return job;
        }
        return job;
    }

    private JSONObject deposit(BankOperation operation) {
        JSONObject job = new JSONObject();
        String account = operation.getAccount();
        if (!map.containsKey(account)) {
            map.put(account, originalAmount);
        }
        long amount = map.get(account);
        long updatedAmount = amount + operation.getValue();
        try {
            map.put(account, updatedAmount);
            job.put("account", account);
            job.put("balance", updatedAmount);
            LOGGER.info("deposit into the account: " + operation.getAccount() + ", original balance: " + amount
                    + ", updated balance: " + updatedAmount);
        } catch (Exception e) {
            LOGGER.warning("Fail to read from " + operation.getAccount());
            job.put("error", "operation fails, please try again");
            return job;
        }
        return job;
    }

    private JSONObject read(BankOperation operation) {
        JSONObject job = new JSONObject();
        String account = operation.getAccount();
        if (!map.containsKey(account)) {
            map.put(account, originalAmount);
        }
        try {
            LOGGER.info("read from account: " + operation.getAccount());
            long value = map.get(account);
            job.put("account", account);
            job.put("balance", value);
        } catch (Exception e) {
            LOGGER.warning("Fail to read from " + operation.getAccount());
            job.put("error", "operation fails, please try again");
            return job;
        }
        return job;
    }

}
