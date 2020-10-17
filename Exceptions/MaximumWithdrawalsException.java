package Exceptions;

public class MaximumWithdrawalsException extends AccountException{
    public MaximumWithdrawalsException() {
        super("You have have reached the maximum number of withdrawals you can make today. Try again tomorrow :)");
    }
}
