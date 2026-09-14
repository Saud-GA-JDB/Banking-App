package Bank.System;

import Bank.Bank;
import Bank.Banking.BankAccount;
import Bank.Banking.Transaction;
import Bank.Cards.Card;
import Bank.Cards.MasterCard;

import java.util.ArrayList;
import java.util.Scanner;

public class Screen {
    public enum Page {START, LOGINCHOICES, LOGINWITHCPR, LOGINWITHCARD, CUSTOMERDASHBOARD,
        BANKERDASHBOARD, CUSTOMERSERVICES, ADDCUSTOMER, CHOOSECUSTOMER, CHOOSEACCOUNT,
        DEPOSITCHOICES, TRANSFERCHOICES, OTHERACCOUNT, WITHDRAW, DEPOSIT, TRANSFER,
        REVIEWTRANSACTION, TRANSACTIONRESULT, BALANCEANDSTATEMENTS, BALANCE, STATEMENT,
        FILTERTRANSACTIONS, CUSTOMDATETIME, ACCOUNTSANDCARDS, ACCOUNTDETAILS, OPENACCOUNT,
        OPENCARD, CARDDETAILS, MESSAGE}
    private Scanner scanner = new Scanner(System.in);
    // TODO
    // need to handle user inputs exceptions
    // maybe create a func that handles them
    private int intInput() {
        int input = scanner.nextInt();
        scanner.nextLine();
//        if (input > choicesNumber || choicesNumber <= 0) throw InputMismatchException;
        return input;
    }

    private String stringInput() {
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
        str.append("0. Back");
        str.append("\n\n");
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
        if (cpr.trim().equalsIgnoreCase("back"))
            return new String[]{cpr};
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
        if (cardNumber.trim().equalsIgnoreCase("back"))
            return new String[]{"back"};
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
        str.append("5. Balance and Statements");
        str.append("\n\n");
        str.append("6. Accounts and Cards");
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
        str.append("0. Back"); // TODO: check if it doesn't cause an error
        str.append("\n\n");
        str.append("User Input: ");
        System.out.println(str);
        return intInput();
    }

    public int bankerDashBoardPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Banker Dashboard");
        str.append("\n\n\n");
        str.append("1. Logout");
        str.append("\n\n");
        str.append("2. Customer Services");
        str.append("\n\n");
        str.append("3. My Banking");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int customerServicesPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Customer Services");
        str.append("\n\n\n");
        str.append("1. Add a customer");
        str.append("\n\n");
        str.append("2. Open an account for a customer");
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    // returns first name, last name, date of birth, cpr, password, security question and answer
    public String[] addCustomerPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Customer Details");
        str.append("\n\n\n");
        str.append("First Name: ");
        System.out.println(str);
        String fName = stringInput();
        System.out.println("\nLast Name: ");
        String lName = stringInput();
        System.out.println("\nDate of Birth (yyyy-MM-dd): ");
        String dateOfBirth = stringInput();
        System.out.println("\nCPR: ");
        String cpr = stringInput();
        System.out.println("\nPassword: ");
        String passWord = stringInput();
        System.out.println("\nSecurity Question: ");
        String securityQuestion = stringInput();
        System.out.println("\nSecurity Question Answer: ");
        String securityQuestionAnswer = stringInput();
        return new String[]{fName, lName, dateOfBirth, cpr, passWord, securityQuestion, securityQuestionAnswer};
    }

    public String chooseCustomerPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Choose a Customer");
        str.append("\n\n\n");
        str.append("Customer CPR: ");
        System.out.println(str);
        return stringInput();
    }

    public int depositChoicesPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Deposit");
        str.append("\n\n\n");
        str.append("1. Deposit into my own account");
        str.append("\n\n");
        str.append("2. Deposit into another customer's account");
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int transferChoicesPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Transfer");
        str.append("\n\n\n");
        str.append("1. Transfer to my own account");
        str.append("\n\n");
        str.append("2. Transfer to another customer's account");
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public String otherAccountPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Transfer To Another Customer's Account");
        str.append("\n\n\n");
        str.append("Destination Account Id: ");
        System.out.println(str);
        return stringInput();
    }

    public String withdrawPage(Bank bank, BankAccount bankAccount) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Withdraw");
        str.append("\n\n\n");
        str.append("From: ").append(bankAccount.getAccountName());
        str.append("\n\n");
        str.append("Balance: $").append(bankAccount.getBalance());
        str.append("\n\n");
        str.append("Amount: ");
        System.out.println(str);
        return stringInput();
    }

    public String depositPage(Bank bank, BankAccount bankAccount) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Deposit");
        str.append("\n\n\n");
        str.append("To: ").append(bankAccount.getAccountName());
        str.append("\n\n");
        str.append("Account Id: ").append(bankAccount.getAccountId());
        str.append("\n\n");
        str.append("Amount: ");
        System.out.println(str);
        return stringInput();
    }

    public String transferPage(Bank bank, BankAccount fromAccount, BankAccount toAccount) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Transfer");
        str.append("\n\n\n");
        str.append("From: ").append(fromAccount.getAccountName());
        str.append("\n\n");
        str.append("To: ").append(toAccount.getAccountName());
        str.append("\n\n");
        str.append("Destination Account Id: ").append(toAccount.getAccountId());
        str.append("\n\n");
        str.append("Balance: $").append(fromAccount.getBalance());
        str.append("\n\n");
        str.append("Amount: ");
        System.out.println(str);
        return stringInput();
    }

    public int reviewTransactionPage(Bank bank, Transaction.TransactionTypes type, String from, String to, double amount, double postTransactionBalance) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Review ").append(type);
        str.append("\n\n\n");
        str.append("From: ").append(from);
        str.append("\n\n");
        str.append("To: ").append(to);
        str.append("\n\n");
        str.append("Amount: $").append(amount);
        str.append("\n\n");
        str.append("Balance after transaction: $").append(postTransactionBalance);
        str.append("\n\n");
        str.append("1. Confirm transaction");
        str.append("\n\n");
        str.append("2. Edit amount"); // TODO: maybe remove, this might complicate things
        str.append("\n\n");
        str.append("0. Cancel");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int transactionResultPage(Bank bank, Transaction transaction) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Transaction Result");
        str.append("\n\n\n");
        str.append("Transaction Id: ").append(transaction.getTransactionId());
        str.append("\n\n");
        str.append("Type: ").append(transaction.getType());
        str.append("\n\n");
        str.append("Date: ").append(transaction.getDate());
        str.append("\n\n");
        str.append("Time: ").append(transaction.getTime());
        str.append("\n\n");
        str.append("From: ").append(transaction.getFromAccountId());
        str.append("\n\n");
        str.append("To: ").append(transaction.getToAccountId());
        str.append("\n\n");
        str.append("Amount: $").append(transaction.getAmount());
        str.append("\n\n");
        str.append("Balance: $").append(transaction.getPostTransactionBalance());
        str.append("\n\n");
        str.append("Successful: ").append(transaction.isSuccessful());
        str.append("\n\n");
        str.append("Note: ").append(transaction.getNote());
        str.append("\n\n");
        str.append("1. Dashboard");
        str.append("\n\n");
        str.append("2. Logout");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int balanceAndStatementsPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Balance and Statements");
        str.append("\n\n\n");
        str.append("1. View balance");
        str.append("\n\n");
        str.append("2. Detailed account statement");
        str.append("\n\n");
        str.append("3. Filter transactions");
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int balancePage(Bank bank, BankAccount bankAccount) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Balance");
        str.append("\n\n\n");
        str.append("Account: ").append(bankAccount.getAccountName());
        str.append("\n\n");
        str.append("Account Id: ").append(bankAccount.getAccountId());
        str.append("\n\n");
        str.append("Balance: $").append(bankAccount.getBalance());
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }
    // TODO: can use later for filtering also but the logic should be in AppSystem
    public int statementPage(Bank bank, BankAccount bankAccount, ArrayList<Transaction> transactions) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Account Statement");
        str.append("\n\n\n");
        str.append("Account: ").append(bankAccount.getAccountName());
        str.append("\n\n");
        str.append("Account Id: ").append(bankAccount.getAccountId());
        str.append("\n\n");
        str.append("Current Balance: $").append(bankAccount.getBalance());
        str.append("\n\n");
        str.append("Transactions: ").append(transactions.size());
        for(Transaction transaction: transactions) {
            str.append("\n\n");
            str.append("Transaction Id: ").append(transaction.getTransactionId());
            str.append("\n");
            str.append("Date: ").append(transaction.getDate());
            str.append("\n");
            str.append("Time: ").append(transaction.getTime());
            str.append("\n");
            str.append("Type: ").append(transaction.getType());
            str.append("\n");
            str.append("From: ").append(transaction.getFromAccountId());
            str.append("\n");
            str.append("To: ").append(transaction.getToAccountId());
            str.append("\n");
            str.append("Amount: $").append(transaction.getAmount());
            str.append("\n");
            str.append("Post-Transaction Balance: $").append(transaction.getPostTransactionBalance());
            str.append("\n");
            str.append("Successful: ").append(transaction.isSuccessful());
            str.append("\n");
            str.append("Note: ").append(transaction.getNote());
        }
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int filterTransactionsPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Filter Transactions");
        str.append("\n\n\n");
        str.append("1. Today");
        str.append("\n\n");
        str.append("2. Yesterday");
        str.append("\n\n");
        str.append("3. Last week");
        str.append("\n\n");
        str.append("4. Last 7 days");
        str.append("\n\n");
        str.append("5. Last month");
        str.append("\n\n");
        str.append("6. Last 30 days");
        str.append("\n\n");
        str.append("7. Custom date and time");
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public String[] customDateTimePage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Custom Date and Time");
        str.append("\n\n\n");
        str.append("Start Date (yyyy-MM-dd): ");
        System.out.println(str);
        String startDate = stringInput();
        System.out.println("\nEnd Date (yyyy-MM-dd): ");
        String endDate = stringInput();
        return new String[]{startDate, endDate};
    }

    public int accountsAndCardsPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Accounts and Cards");
        str.append("\n\n\n");
        str.append("1. View account details");
        str.append("\n\n");
        str.append("2. Open an account for myself");
        str.append("\n\n");
        str.append("3. View card details");
        str.append("\n\n");
        str.append("4. Open a card");
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int accountDetailsPage(Bank bank, BankAccount bankAccount) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Account Details");
        str.append("\n\n\n");
        str.append("Account: ").append(bankAccount.getAccountName());
        str.append("\n\n");
        str.append("Account Id: ").append(bankAccount.getAccountId());
        str.append("\n\n");
        str.append("Type: ").append(bankAccount.getType());
        str.append("\n\n");
        str.append("Balance: $").append(bankAccount.getBalance());
        str.append("\n\n");
        str.append("Date Created: ").append(bankAccount.getDateCreated());
        str.append("\n\n");
        str.append("Active: ").append(bankAccount.isActive());
        str.append("\n\n");
        str.append("Overdraft Fee: $").append(bankAccount.getOverDraftFee());
        str.append("\n\n");
        str.append("Overdraft Count: ").append(bankAccount.getOverDraftCount());
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public String[] openAccountPage(Bank bank) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Open an Account");
        str.append("\n\n\n");
        str.append("Account Name: ");
        System.out.println(str);
        String accountName = stringInput();
        System.out.println("\nAccount Type: ");
        System.out.println("\n1. Checking");
        System.out.println("\n2. Savings");
        System.out.println("\nUser Input: ");
        String accountType = stringInput();
        return new String[]{accountName, accountType};
    }

    public String[] openCardPage(Bank bank, BankAccount bankAccount) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Open a Card");
        str.append("\n\n\n");
        str.append("Account: ").append(bankAccount.getAccountName());
        str.append("\n\n");
        str.append("1. MasterCard");
        str.append("\n\n");
        str.append("2. PlatinumCard");
        str.append("\n\n");
        str.append("3. TitaniumCard");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.println(str);
        String cardType = stringInput();
        System.out.println("\nPasscode (6 digits): ");
        String passCode = stringInput();
        return new String[]{cardType, passCode};
    }

    public int cardDetailsPage(Bank bank, BankAccount bankAccount, Card card, Card.CardTypes cardType) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n");
        str.append("\t\t").append("Card Details");
        str.append("\n\n\n");
        str.append("Account: ").append(bankAccount.getAccountName());
        str.append("\n\n");
        str.append("Card Number: ").append(card.getCardNumber());
        str.append("\n\n");
        str.append("Card Type: ").append(cardType);
        str.append("\n\n");
        str.append("Daily Withdraw Limit: $").append(card.getWithdrawLimit());
        str.append("\n\n");
        str.append("Daily Transfer Limit: $").append(card.getTransferLimit());
        str.append("\n\n");
        str.append("Daily Transfer Limit (Own Account): $").append(card.getTransferLimitOwnAccount());
        str.append("\n\n");
        str.append("Daily Deposit Limit: $").append(card.getDepositLimit());
        str.append("\n\n");
        str.append("Daily Deposit Limit (Own Account): $").append(card.getDepositLimitOwnAccount());
        str.append("\n\n");
        str.append("Amount Withdrawn Today: $").append(card.getAmountWithdrawnToday());
        str.append("\n\n");
        str.append("Amount Transferred Today: $").append(card.getTransferredWithdrawnToday());
        str.append("\n\n");
        str.append("Amount Transferred to Own Account Today: $").append(card.getAmountTransferredToOwnAccountToday());
        str.append("\n\n");
        str.append("Amount Deposited Today: $").append(card.getAmountDepositedToday());
        str.append("\n\n");
        str.append("Amount Deposited to Own Account Today: $").append(card.getAmountDepositedToOwnAccountToday());
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
        return intInput();
    }

    public int messagePage(Bank bank, String message) {
        StringBuilder str = new StringBuilder();
        str.append("\t\t").append(bank.getName());
        str.append("\n\n\n");
        str.append(message);
        str.append("\n\n");
        str.append("0. Back");
        str.append("\n\n");
        str.append("User Input: ");
        System.out.print(str);
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
        screen.loginWithCardPage(bank);
        clearConsole();
        screen.customerDashBoardPage(bank);
        clearConsole();
        BankAccount acc1 = new BankAccount("main1", BankAccount.Type.CHECKING);
        BankAccount acc2 = new BankAccount("main2", BankAccount.Type.SAVINGS);
        acc1.setBalance(1000);
        acc2.setBalance(500);
        ArrayList<BankAccount> bankAccounts = new ArrayList<BankAccount>();
        bankAccounts.add(acc1); bankAccounts.add(acc2);
        screen.chooseAccountPage(bank, bankAccounts);
        clearConsole();
        screen.depositChoicesPage(bank);
        clearConsole();
        screen.depositPage(bank, acc1);
        clearConsole();
        screen.reviewTransactionPage(bank, Transaction.TransactionTypes.DEPOSIT, "Cash", acc1.getAccountName(), 150, 1150);
        clearConsole();
        screen.withdrawPage(bank, acc1);
        clearConsole();
        screen.reviewTransactionPage(bank, Transaction.TransactionTypes.WITHDRAW, acc1.getAccountName(), "Cash", 150, 850);
        clearConsole();
        screen.transferChoicesPage(bank);
        clearConsole();
        screen.otherAccountPage(bank);
        clearConsole();
        screen.transferPage(bank, acc1, acc2);
        clearConsole();
        screen.reviewTransactionPage(bank, Transaction.TransactionTypes.TRANSFER, acc1.getAccountName(), acc2.getAccountName(), 150, 850);
        clearConsole();
        Transaction transaction1 = new Transaction(150, Transaction.TransactionTypes.TRANSFER, acc1.getAccountId(), acc2.getAccountId(), 850, "Successful");
        transaction1.setSuccessful(true);
        acc1.setBalance(850);
        acc2.setBalance(650);
        screen.transactionResultPage(bank, transaction1);
        clearConsole();
        Transaction transaction2 = new Transaction(6000, Transaction.TransactionTypes.WITHDRAW, acc1.getAccountId(), null, 850, "Daily withdraw limit exceeded");
        screen.transactionResultPage(bank, transaction2);
        clearConsole();
        screen.balanceAndStatementsPage(bank);
        clearConsole();
        screen.balancePage(bank, acc1);
        clearConsole();
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction1); transactions.add(transaction2);
        screen.statementPage(bank, acc1, transactions);
        clearConsole();
        screen.filterTransactionsPage(bank);
        clearConsole();
        screen.customDateTimePage(bank);
        clearConsole();
        screen.accountsAndCardsPage(bank);
        clearConsole();
        screen.accountDetailsPage(bank, acc1);
        clearConsole();
        screen.openAccountPage(bank);
        clearConsole();
        screen.openCardPage(bank, acc1);
        clearConsole();
        MasterCard card = new MasterCard("demoHashedCode");
        screen.cardDetailsPage(bank, acc1, card, Card.CardTypes.MASTERCARD);
        clearConsole();
        screen.bankerDashBoardPage(bank);
        clearConsole();
        screen.customerServicesPage(bank);
        clearConsole();
        screen.addCustomerPage(bank);
        clearConsole();
        screen.chooseCustomerPage(bank);
        clearConsole();
        screen.openAccountPage(bank);
        clearConsole();
        screen.messagePage(bank, "Account opened successfully");
        clearConsole();
        screen.chooseAccountPage(bank, new ArrayList<BankAccount>());
        clearConsole();
        screen.statementPage(bank, acc1, new ArrayList<Transaction>());
    }

}
