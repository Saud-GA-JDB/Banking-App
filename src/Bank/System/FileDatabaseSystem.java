package Bank.System;

import Bank.Banking.BankAccount;
import Bank.Banking.Transaction;
import Bank.Cards.Card;
import Bank.Users.User;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.stream.Stream;

public class FileDatabaseSystem {
    private final static Path baseDir = Paths.get("database");
    private final static Path usersAndAccountDir = baseDir.resolve("usersAndAccount");
    private final static Path cprsAndAccountsAndCardsFile = usersAndAccountDir.resolve("cprsAndAccountsAndCards.txt");
    private final static Path customersCprAndAccountDir = usersAndAccountDir.resolve("customersCprAndAccount");
    private final static Path bankersCprAndAccountDir = usersAndAccountDir.resolve("bankersCprAndAccount");
    private final static Path peopleTransactionsDir = baseDir.resolve("usersTransactions");
    private final static Path customersTransactionsDir = peopleTransactionsDir.resolve("customersTransactions");
    private final static Path bankersTransactionsDir = peopleTransactionsDir.resolve("bankersTransactions");
    private final static Path customersCprsFile = customersCprAndAccountDir.resolve("customersCprs.txt");
    private final static Path bankersCprsFile = bankersCprAndAccountDir.resolve("bankersCprs.txt");


    public static void setupApplicationDirectories() throws IOException {
        // cprs and accounts db dir initialization
//        Files.createDirectories(usersAndAccountDir);
        Files.createDirectories(customersCprAndAccountDir);
        Files.createDirectories(bankersCprAndAccountDir);

        if (!Files.exists(cprsAndAccountsAndCardsFile)) Files.createFile(cprsAndAccountsAndCardsFile);
        if (!Files.exists(bankersCprsFile)) Files.createFile(bankersCprsFile);
        if (!Files.exists(customersCprsFile)) Files.createFile(customersCprsFile);
        // transactions db dir initialization
        Files.createDirectories(customersTransactionsDir);
        Files.createDirectories(bankersTransactionsDir);
    }

    public static void createUserAccountDir(String cpr, User.Role role) throws IOException {
        if (role.equals(User.Role.CUSTOMER)) {
            Files.createDirectories(customersTransactionsDir.resolve(cpr));
        } else if (role.equals(User.Role.BANKER)) {
            Files.createDirectories(bankersTransactionsDir.resolve(cpr));
        }
    }

    public static void createUserTransactionsDir(String cpr, User.Role role) throws IOException {
        Path relativeBase = null;
        if (role.equals(User.Role.CUSTOMER)) {
            relativeBase = customersTransactionsDir.resolve(cpr);
        } else if (role.equals(User.Role.BANKER)) {
            relativeBase = bankersTransactionsDir.resolve(cpr);
        }
        Files.createDirectories(relativeBase.resolve("checking"));
        Files.createDirectories(relativeBase.resolve("savings"));

        // maybe also create a user.properties with important info of the user
        // like cpr, name, bank accounts names and ids, balance in each account, cards, etc...
    }

    public static void createUserBankAccountTransactionsDir(String cpr, User.Role role, BankAccount.Type bankAccountType, String accountName) throws IOException {
        Path relativeBase = null;
        if (role.equals(User.Role.CUSTOMER)) {
            relativeBase = customersTransactionsDir.resolve(cpr);
        } else if (role.equals(User.Role.BANKER)) {
            relativeBase = bankersTransactionsDir.resolve(cpr);
        }

        if (bankAccountType.equals(BankAccount.Type.CHECKING)) {
            relativeBase = relativeBase.resolve(bankAccountType.toString().toLowerCase());
        } else if (bankAccountType.equals(BankAccount.Type.SAVINGS)) {
            relativeBase = relativeBase.resolve(bankAccountType.toString().toLowerCase());
        }
        Files.createDirectories(relativeBase.resolve(accountName));
        // it should also create a info.txt file with info about this bank account NOT transactions
        // note that account name should be unique inside each user.
    }

    // TODO: maybe remove these
    public static boolean addUser(String cpr, String name,User.Role role) throws IOException {
        // make sure cpr doesn't already exist.
        if (userExist(cpr, role)) return false;

        StringBuilder stringBuilder = new StringBuilder(cpr).append(",").append(name).append("\n");
        Path path = null;
        if (role == User.Role.CUSTOMER) {
            path = customersCprsFile;
        } else if (role == User.Role.BANKER) {
            path = bankersCprsFile;
        }
        Files.writeString(path, stringBuilder, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        return true;
    }

    public static boolean addUserToCprsAndAccountsAndCardsFile(String cpr,String hashedPassword,String fullName ,User.Role role) throws IOException {
        if (userExist(cpr, role)) return false;
        StringBuilder str = new StringBuilder(cpr).append(",").append(hashedPassword).append(",").append(fullName).append(",").append(role);
        Files.writeString(cprsAndAccountsAndCardsFile, str);
        return true;
    }

    // the basic functionality is taken from https://www.baeldung.com/java-modify-file-content-based-on-pattern
    public static boolean addBankAccountToCprsAndAccountsAndCardsFile(String cpr, String bankAccountName) throws IOException {
        if (!userExist(cpr)) return false;
        Path modifiedFile = usersAndAccountDir.resolve("modified.txt");

        try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile);
             BufferedWriter writer = Files.newBufferedWriter(modifiedFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(cpr)) {
                    StringBuilder replaced = new StringBuilder(line);
                    replaced.append(",").append("#bankAccountName:").append(bankAccountName);
                    writer.write(replaced.toString());
                }
            }

        }

        Files.move(modifiedFile, cprsAndAccountsAndCardsFile, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public static boolean cardNumberDoesntExist(long cardNumber) {
        long count = 0; // not int bc/ for some reason it returns long and not int
        try (Stream<String> linesStream = Files.lines(cprsAndAccountsAndCardsFile)) {
            count = linesStream.filter(line -> line.contains(Long.toString(cardNumber))).count();
        } catch (IOException e) {e.printStackTrace();}
        return count != 0;
    }

    public static boolean addCardToCprsAndAccountsAndCardsFile(String cpr, String bankAccountName, long cardNumber, Card.CardTypes cardType, String hashedCode) throws IOException {
        if (!userExist(cpr)) return false;
        Path modifiedFile = usersAndAccountDir.resolve("modified.txt");

        boolean found = false;

        try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile);
             BufferedWriter writer = Files.newBufferedWriter(modifiedFile)) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.contains(bankAccountName) && line.contains(bankAccountName)) {
                    StringBuilder replaced = new StringBuilder(line);
                    String[] strArr = line.split(",");
                    int i=0; int index = 0;
                    for (String str: strArr) {
                        if (str.contains(bankAccountName))
                            index=i;
                        i++;
                    }
                    //check if a card is already associated with the bank account.
                    if (strArr[index].contains("cardNumber"))
                        return false;

//                    String[] strArr2 = strArr[index].split("#accName:"+bankAccountName);
                    String temp = strArr[index] + "#cardNumber:" + cardNumber + "cardType:" + cardType + "#hashedCode:" + hashedCode;
                    strArr[index] = temp;
                    // reconstruct the line
                    for (String str: strArr) replaced.append(str);

                    writer.write(replaced.toString());
                } else return false;

            }
        }

        Files.move(modifiedFile, cprsAndAccountsAndCardsFile, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public static void addTransaction(String cpr, User.Role role, BankAccount bankAccount, Transaction transaction) throws IOException {
        Path relativeBase = null;
        if (role == User.Role.CUSTOMER) {
            relativeBase = customersTransactionsDir.resolve(cpr);
        } else if (role == User.Role.BANKER) {
            relativeBase = bankersTransactionsDir.resolve(cpr);
        }

        BankAccount.Type bankAccountType = bankAccount.getType();

        if (bankAccountType == BankAccount.Type.CHECKING) {
            relativeBase = relativeBase.resolve(bankAccountType.toString().toLowerCase());
        } else if (bankAccountType == BankAccount.Type.SAVINGS) {
            relativeBase = relativeBase.resolve(bankAccountType.toString().toLowerCase());
        }
        relativeBase = relativeBase.resolve(bankAccount.getAccountName());
        String year = Integer.toString(transaction.getDate().getYear());
        String month = Integer.toString(transaction.getDate().getMonthValue());
        relativeBase = relativeBase.resolve(year).resolve(month);
        // if the dir doesn't exist it will create it otherwise it won't do anything
        Files.createDirectories(relativeBase);

        String transactionStr = transactionStrBuilder(transaction);

        relativeBase = relativeBase.resolve("transactions.txt");

        // Appends text. Creates the file first if it doesn't exist.
        Files.writeString(relativeBase, transactionStr, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static String transactionStrBuilder(Transaction transaction) {
        StringBuilder stringBuilder = new StringBuilder("############\nTransaction Id: " + transaction.getTransactionId());
        stringBuilder.append("\nTransfer Id: ").append(transaction.getTransferId());
        stringBuilder.append("\namount: ").append(transaction.getAmount());
        stringBuilder.append("\ndate: ").append(transaction.getDate());
        stringBuilder.append("\ntime: ").append(transaction.getTime());
        stringBuilder.append("\ntransaction type: ").append(transaction.getType());
        stringBuilder.append("\nfrom: ").append(transaction.getFromAccountId());
        stringBuilder.append("\nto: ").append(transaction.getToAccountId());
        stringBuilder.append("\nbalance: ").append(transaction.getPostTransactionBalance());
        stringBuilder.append("\nis successful: ").append(transaction.isSuccessful());
        stringBuilder.append("\nnote: ").append(transaction.getNote());
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static boolean userExist(String cpr, User.Role role) throws IOException{
        Path path = null;
        if (role == User.Role.BANKER) {
            path = bankersCprsFile;
        } else if (role == User.Role.CUSTOMER) {
            path = customersCprsFile;
        }
        long count = 0; // not int bc/ for some reason it returns long and not int
        try (Stream<String> linesStream = Files.lines(path)) {
            count = linesStream.filter(line -> line.contains(cpr)).count();
        }
        return count != 0;
    }

    public static boolean userExist(String cpr) throws IOException{
        long count = 0; // not int bc/ for some reason it returns long and not int
        try (Stream<String> linesStream = Files.lines(cprsAndAccountsAndCardsFile)) {
            count = linesStream.filter(line -> line.contains(cpr)).count();
        }
        return count != 0;
    }






    // revisit later to solve this. after finishing all main requirements
    // I can use an emun with all IOFunctions in the function argument and then
    // put switch statement based on that what function will be called.
//    public static void handleIOExceptions (Function ioFunc) {
//      try {
//          ioFunc()
//      } catch (IOException e) {e.printStackTrace();}
//    }

    public static void main(String[] args) {
        try {
//            setupApplicationDirectories();
//            createUserTransactionsDir("040206343", User.Role.CUSTOMER);
//            createUserTransactionsDir("040206343", User.Role.BANKER);
//            createUserBankAccountTransactionsDir("040206343", User.Role.BANKER , BankAccount.Type.SAVINGS, "saudMain");
//            addUser("040206343", "Saud Salah Al-Ansari Al-Khazriji", User.Role.BANKER);
            BankAccount bankAccount = new BankAccount("saudMain", BankAccount.Type.SAVINGS);
            Transaction transaction = new Transaction(50.5, Transaction.TransactionTypes.DEPOSIT, bankAccount.getAccountId(), UUID.randomUUID(), 60, "Successful");
//            transaction.setSuccessful(true);
//            addTransaction("040206343", User.Role.BANKER, bankAccount, transaction);
//            System.out.println(userExist("040206343", User.Role.BANKER));
            addUserToCprsAndAccountsAndCardsFile("040206343","dsakljdas", "Saud Salah", User.Role.BANKER);
            addBankAccountToCprsAndAccountsAndCardsFile("040206343", bankAccount.getAccountName());
        } catch (IOException e) {e.printStackTrace();}
    }
}
