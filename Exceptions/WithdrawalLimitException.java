package Exceptions;

public class WithdrawalLimitException extends AccountException{
    public WithdrawalLimitException() {
        super("You cannot withdraw this much amount.");
    }
}
