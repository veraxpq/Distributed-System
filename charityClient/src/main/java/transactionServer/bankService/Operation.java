package transactionServer.bankService;

import java.io.Serializable;

/**
 * This is the enum class of the operation, including three types of operations, read, withdraw and deposit.
 */
public enum Operation implements Serializable {
    READ, WITHDRAW, DEPOSIT;
}
