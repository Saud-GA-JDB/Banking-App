package Bank.System;

import Bank.Banking.BankAccount;
import Bank.Users.User;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDatabaseSystem {
    private final static Path baseDir = Paths.get("database");
    private final static Path usersAndAccountDir = baseDir.resolve("usersAndAccount");
    private final static Path customersCprAndAccountDir = usersAndAccountDir.resolve("customersCprAndAccount");
    private final static Path bankersCprAndAccountDir = usersAndAccountDir.resolve("bankersCprAndAccount");
    private final static Path peopleTransactionsDir = baseDir.resolve("usersTransactions");
    private final static Path customersTransactionsDir = peopleTransactionsDir.resolve("customersTransactions");
    private final static Path bankersTransactionsDir = peopleTransactionsDir.resolve("bankersTransactions");


    public static void setupApplicationDirectories() throws IOException {
        // cprs and accounts db dir initialization
//        Files.createDirectories(usersAndAccountDir);
        Files.createDirectories(customersCprAndAccountDir);
        Files.createDirectories(bankersCprAndAccountDir);
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
    }

    public static void createUserBankAccountTransactionsDir(String cpr, User.Role role, BankAccount.Type bankType, String accountName) throws IOException {
        Path relativeBase = null;
        if (role.equals(User.Role.CUSTOMER)) {
            relativeBase = customersTransactionsDir.resolve(cpr);
        } else if (role.equals(User.Role.BANKER)) {
            relativeBase = bankersTransactionsDir.resolve(cpr);
        }

        if (bankType.equals(BankAccount.Type.CHECKING)) {
            relativeBase = relativeBase.resolve(bankType.toString().toLowerCase());
        } else if (bankType.equals(BankAccount.Type.SAVINGS)) {
            relativeBase = relativeBase.resolve(bankType.toString().toLowerCase());
        }
        Files.createDirectories(relativeBase.resolve(accountName));
        // it should also create a info.txt file with info about this bank account NOT transactions
        // note that account name should be unique inside each user.
    }


    /*
    TODO
    need to create the methods for transactions later
     */



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
//            createUserTransactionsDir("040206343", AppSystem.Role.CUSTOMER);
//            createUserTransactionsDir("040206343", User.Role.BANKER);
            createUserBankAccountTransactionsDir("040206343", User.Role.BANKER , BankAccount.Type.SAVINGS, "saudMain");
        } catch (IOException e) {e.printStackTrace();}
    }
}
