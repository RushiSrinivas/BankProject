package Exceptions;

public class OverdraftException extends AccountException{
    public OverdraftException() {
        super("Withdrawing money from the overdraft limit");
    }
}
