package transactionServer.bankService;

import java.io.Serializable;

/**
 * This class represents an operation of a bank.
 */
public class BankOperation implements Serializable {
    private Operation operation;
    private String account;
    private Long value;

    /**
     * This is the constructor of the BankOperation class, initializing the object.
     *
     * @param operation The given operation.
     * @param account The given name of the account.
     */
    public BankOperation(Operation operation, String account) {
        this.operation = operation;
        this.account = account;
    }

    /**
     * This is the constructor of the BankOperation class.
     *
     * @param operation The given operation object.
     * @param account the given name of the account.
     * @param value the given value of the operation.
     */
    public BankOperation(Operation operation, String account, long value) {
        this.operation = operation;
        this.account = account;
        this.value = value;
    }

    /**
     * This method returns the operation object.
     *
     * @return the operation object.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * This method returns the account.
     *
     * @return the account in the bank operation object.
     */
    public String getAccount() {
        return account;
    }

    /**
     * This method returns the value of the bank operation object.
     *
     * @return the value of the bank operation object.
     */
    public long getValue() {
        return value;
    }

    /**
     * This method returns the string of the object.
     *
     * @return the string of the object.
     */
    public String toString() {
        String str = operation.toString() + " " + account;
        return value == null ? str : str + " " + value.longValue();
    }
}
