package main;

import account.Account;
import account.Savings;

import java.util.Scanner;

public class CalculateInterest {
    public static void main(String[] args) {
        System.out.print("Login: ");

        Scanner scanner = new Scanner(System.in);
        int accNo = scanner.nextInt();

        if (Account.login(accNo)) {
            if (Account.accType.equals("Current")) return;
            Savings savings = new Savings();
            savings.calculateInterest();
        }
    }
}
