package Exceptions;

public class BelowMinimumBalanceException extends AccountException{
    public BelowMinimumBalanceException(double total, String account) {
        super(total + " is less than the minimum balance for a " + account + " account.");
    }
}
