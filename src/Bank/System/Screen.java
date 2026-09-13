package Bank.System;

import Bank.Bank;
import Bank.Banking.BankAccount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Screen {
    public enum Page {START, LOGINCHOICES}
    // TODO
    // need to handle user inputs exceptions
    // maybe create a func that handles them
    private int intInput() {
        Scanner scanner = new Scanner(System.in);
        int input = scanner.nextInt();
//        if (input > choicesNumber || choicesNumber <= 0) throw InputMismatchException;
        return input;
    }

    private String stringInput() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        return input;
    }

    public int startPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("Welcome to ").append(bank.getName());
        str.append("\n\n\n");
        str.append("1. ").append("Start");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int loginChoicesPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n\n\n");
        str.append("1. Login with CPR and password");
        str.append("\n\n");
        str.append("2. Login with card and passcode");
        str.append("\n\n");
        str.append("3. Open an account");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public String[] loginWithCpr(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Login With CPR Page");
        str.append("\n\n\n");
        str.append("CPR: ");
        System.out.println(str);
        String cpr = stringInput();
        System.out.println("\nPassword: ");
        String passWord = stringInput();
        return new String[]{cpr, passWord};
    }

    public String[] loginWithCardPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Login With Card Page");
        str.append("\n\n\n");
        str.append("Card Number: ");
        System.out.println(str);
        String cardNumber = stringInput();
        System.out.println("\nPasscode: ");
        String passCode = stringInput();
        return new String[]{cardNumber, passCode};
    }

    public int customerDashBoardPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Dashboard");
        str.append("\n\n\n");
        str.append("1. Logout");
        str.append("\n\n");
        str.append("2. Deposit");
        str.append("\n\n");
        str.append("3. Withdraw");
        str.append("\n\n");
        str.append("4. Transfer");
        str.append("\n\n");
        str.append("4. Balance and Statements");
        str.append("\n\n");
        str.append("6. Accounts and Cards"); // handle later like opening a new bankAccount/Card
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int chooseAccountPage(Bank bank, ArrayList<BankAccount> bankAccounts) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Choose an account: ");
        int i=1;
        for(BankAccount bankAccount: bankAccounts) {
            str.append("\n\n");
            str.append(i).append(". ").append(bankAccount.getAccountName());
            i++;
        }
        str.append("\n\n");
        str.append("User Input: ");
        System.out.println(str);
        return intInput();
    }

    public static void clearConsole() {
        for (int i=0; i<100; i++)
            System.out.println();
    }
// these don't work from my understanding it just cause of the IDE. retry later in the cmd
//    public static void clearConsole2() {
//        try {
//            final String os = System.getProperty("os.name");
//
//            if (os.contains("Windows")) {
//                // Windows: Executes the built-in 'cls' command inside cmd.exe
//                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
//            } else {
//                // Linux/macOS: Uses standard ANSI escape codes
//                System.out.print("\033[H\033[2J");
//                System.out.flush();
//            }
//        } catch (final Exception e) {
//            // Fallback: Handle potential exceptions smoothly
//            System.out.println("Could not clear the console.");
//        }
//    }
//
//    public static void clearWithWindowsCommand() {
//        try {
//            new ProcessBuilder("cmd", "/c", "cls")
//                    .inheritIO()
//                    .start()
//                    .waitFor();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }



    public static void main(String[] args) {
        System.out.println("uahfuhduhsudkhfsdkh");
        clearConsole();
        Bank bank = new Bank("BBK");
        Screen screen = new Screen();
        screen.startPage(bank);
        clearConsole();
        screen.loginChoicesPage(bank);
        clearConsole();
        screen.loginWithCpr(bank);
        clearConsole();
        screen.customerDashBoardPage(bank);
        clearConsole();
        BankAccount acc1 = new BankAccount("main1", BankAccount.Type.CHECKING);
        BankAccount acc2 = new BankAccount("main2", BankAccount.Type.CHECKING);
        ArrayList<BankAccount> bankAccounts = new ArrayList<BankAccount>();
        bankAccounts.add(acc1); bankAccounts.add(acc2);
        screen.chooseAccountPage(bank, bankAccounts);
    }

}
