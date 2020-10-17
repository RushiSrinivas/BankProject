package Exceptions;

public class BelowZeroBalanceException extends AccountException {

    public BelowZeroBalanceException() {
        super("Balance after transaction is less than 0. Cannot complete transaction");
    }
}
