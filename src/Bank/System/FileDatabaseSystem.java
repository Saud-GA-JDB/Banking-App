package Bank.System;

import Bank.Banking.BankAccount;
import Bank.Banking.Transaction;
import Bank.Cards.Card;
import Bank.Cards.PlatinumCard;
import Bank.Users.User;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        for (BankAccount.Type type : BankAccount.Type.values()) {
            Path accountDir = relativeBase.resolve(type.toString().toLowerCase()).resolve(accountName);
            if (type != bankAccountType && Files.exists(accountDir)) {
                throw new FileAlreadyExistsException("Account name already exists for this user: " + accountName);
            }
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

    public static void addOrUpdateBankAccountPropertiesFile(BankAccount bankAccount, String cpr, User.Role role) throws IOException {
        Path relativeBase = null;
        if (role == User.Role.CUSTOMER) {
            relativeBase = customersTransactionsDir.resolve(cpr);
        } else if (role == User.Role.BANKER) {
            relativeBase = bankersTransactionsDir.resolve(cpr);
        }

        BankAccount.Type bankAccountType = bankAccount.getType();
        Path oldAccountDir = null;
        if (Files.isDirectory(relativeBase)) {
            try (Stream<Path> filesStream = Files.walk(relativeBase, 3)) {
                Path[] accountFiles = filesStream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().equals(bankAccount.getAccountId() + ".properties")).toArray(Path[]::new);
                if (accountFiles.length > 1) throw new IOException("Account Id exists in more than one folder: " + bankAccount.getAccountId());
                if (accountFiles.length == 1) oldAccountDir = accountFiles[0].getParent();
            }
        }

        if (bankAccountType == BankAccount.Type.CHECKING) {
            relativeBase = relativeBase.resolve(bankAccountType.toString().toLowerCase());
        } else if (bankAccountType == BankAccount.Type.SAVINGS) {
            relativeBase = relativeBase.resolve(bankAccountType.toString().toLowerCase());
        }
        relativeBase = relativeBase.resolve(bankAccount.getAccountName());
        if (Files.isDirectory(relativeBase)) {
            // this try is chatgpt generated
            try (Stream<Path> filesStream = Files.list(relativeBase)) {
                boolean accountExists = filesStream.anyMatch(path -> path.toString().endsWith(".properties") && !path.getFileName().toString().equals(bankAccount.getAccountId() + ".properties"));
                if (accountExists) throw new FileAlreadyExistsException("Account name already belongs to another account: " + bankAccount.getAccountName());
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder stringBuilder = new StringBuilder("accountId=" + bankAccount.getAccountId());
        stringBuilder.append("\naccountName=").append(bankAccount.getAccountName());
        stringBuilder.append("\ntype=").append(bankAccount.getType());
        stringBuilder.append("\nbalance=").append(bankAccount.getBalance());
        stringBuilder.append("\ndateCreated=").append(dateFormat.format(bankAccount.getDateCreated()));
        stringBuilder.append("\nisActive=").append(bankAccount.isActive());
        stringBuilder.append("\noverDraftFee=").append(bankAccount.getOverDraftFee());
        stringBuilder.append("\noverDraftCount=").append(bankAccount.getOverDraftCount());
        stringBuilder.append("\n");
        // this if statement is chatgpt generated
        if (oldAccountDir != null && !oldAccountDir.toString().equals(relativeBase.toString())) {
            // read first so an incomplete account file cannot be renamed
            getBankAccountFromFile(oldAccountDir.getFileName().toString(), cpr, role);
            renameBankAccountDir(oldAccountDir, relativeBase, bankAccount, cpr, stringBuilder.toString());
            return;
        }
        createUserBankAccountTransactionsDir(cpr, role, bankAccountType, bankAccount.getAccountName());
        relativeBase = relativeBase.resolve(bankAccount.getAccountId() + ".properties");
        Files.writeString(relativeBase, stringBuilder);
    }
    // this method is chatgpt generated.
    private static void renameBankAccountDir(Path oldAccountDir, Path newAccountDir, BankAccount bankAccount, String cpr, String accountDetails) throws IOException {
        Path userDir = oldAccountDir.getParent().getParent().toAbsolutePath().normalize();
        if (!newAccountDir.toAbsolutePath().normalize().startsWith(userDir)) {
            throw new IOException("The renamed account must stay inside the user's directory");
        }
        for (BankAccount.Type type : BankAccount.Type.values()) {
            Path accountDir = userDir.resolve(type.toString().toLowerCase()).resolve(bankAccount.getAccountName());
            if (Files.exists(accountDir) && !Files.isSameFile(accountDir, oldAccountDir)) {
                throw new FileAlreadyExistsException("Account name already exists for this user: " + bankAccount.getAccountName());
            }
        }

        String oldAccountPrefix = "#bankAccountName:" + oldAccountDir.getFileName();
        String newAccountPrefix = "#bankAccountName:" + bankAccount.getAccountName();
        String fileName = bankAccount.getAccountId() + ".properties";
        String oldAccountDetails = Files.readString(oldAccountDir.resolve(fileName));
        Path modifiedFile = null;
        boolean moved = false;
        try {
            if (Files.exists(cprsAndAccountsAndCardsFile)) {
                modifiedFile = Files.createTempFile(usersAndAccountDir, "accounts-", ".tmp");
                try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile);
                     BufferedWriter writer = Files.newBufferedWriter(modifiedFile)) {
                    String line;
                    boolean found = false;
                    while ((line = reader.readLine()) != null) {
                        String[] fields = line.split(",", -1);
                        if (fields[0].equals(cpr)) {
                            for (int i = 4; i < fields.length; i++) {
                                String account = fields[i];
                                if (account.equals(oldAccountPrefix) || account.startsWith(oldAccountPrefix + "#")) {
                                    if (found) throw new IOException("Multiple entries found for the account being renamed");
                                    String[] properties = account.split("#", -1);
                                    StringBuilder replaced = new StringBuilder(newAccountPrefix).append("#bankAccountType:").append(bankAccount.getType());
                                    for (int j = 2; j < properties.length; j++) {
                                        if (!properties[j].startsWith("bankAccountType:")) replaced.append("#").append(properties[j]);
                                    }
                                    fields[i] = replaced.toString();
                                    found = true;
                                } else if (account.equals(newAccountPrefix) || account.startsWith(newAccountPrefix + "#")) {
                                    throw new FileAlreadyExistsException("Account name already exists for this user: " + bankAccount.getAccountName());
                                }
                            }
                            line = String.join(",", fields);
                        }
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }

            Files.createDirectories(newAccountDir.getParent());
            Files.move(oldAccountDir, newAccountDir);
            moved = true;
            Files.writeString(newAccountDir.resolve(fileName), accountDetails);
            if (modifiedFile != null) Files.move(modifiedFile, cprsAndAccountsAndCardsFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            if (moved) {
                try {
                    Files.writeString(newAccountDir.resolve(fileName), oldAccountDetails);
                    Files.move(newAccountDir, oldAccountDir);
                } catch (IOException restoreError) {
                    e.addSuppressed(restoreError);
                }
            }
            throw e;
        } finally {
            if (modifiedFile != null) Files.deleteIfExists(modifiedFile);
        }
    }

    public static BankAccount getBankAccountFromFile(String bankAccountName, String cpr, User.Role role) throws IOException {
        Path relativeBase = null;
        if (role == User.Role.CUSTOMER) {
            relativeBase = customersTransactionsDir.resolve(cpr);
        } else if (role == User.Role.BANKER) {
            relativeBase = bankersTransactionsDir.resolve(cpr);
        }
        // -------------- this part is generated by ChatGPT  --------------//
        if (Files.isDirectory(relativeBase.resolve("checking").resolve(bankAccountName)) && Files.isDirectory(relativeBase.resolve("savings").resolve(bankAccountName))) {
            throw new IOException("Account name exists under both checking and savings: " + bankAccountName);
        }

        for (BankAccount.Type bankAccountType : BankAccount.Type.values()) {
            Path accountDir = relativeBase.resolve(bankAccountType.toString().toLowerCase()).resolve(bankAccountName);
            if (!Files.isDirectory(accountDir)) continue; //if said dir doesnt exist then check the other BankAccount type

            Path accountFile;
            try (Stream<Path> filesStream = Files.list(accountDir)) {
                Path[] accountFiles = filesStream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".properties")).toArray(Path[]::new);
                if (accountFiles.length > 1) throw new IOException("Multiple bank account properties files found: " + accountDir);
                accountFile = accountFiles.length == 0 ? null : accountFiles[0];
            }
            if (accountFile == null) continue;

            BankAccount bankAccount = new BankAccount(bankAccountName, bankAccountType);
            ArrayList<String> propertiesRead = new ArrayList<String>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
        // --------------- end of the ChatGPT generated code ---------------//
            try (BufferedReader reader = Files.newBufferedReader(accountFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("=", 2);
                    if (fields.length != 2 || fields[1].isEmpty()) throw new IOException("Incomplete bank account property: " + line);
                    if (propertiesRead.contains(fields[0])) throw new IOException("Duplicate bank account property: " + fields[0]);
                    propertiesRead.add(fields[0]);
                    if (fields[0].equals("accountId")) {
                        bankAccount.setAccountId(UUID.fromString(fields[1]));
                    } else if (fields[0].equals("accountName")) {
                        bankAccount.setAccountName(fields[1]);
                    } else if (fields[0].equals("type")) {
                        bankAccount.setType(BankAccount.Type.valueOf(fields[1]));
                    } else if (fields[0].equals("balance")) {
                        bankAccount.setBalance(Double.parseDouble(fields[1]));
                    } else if (fields[0].equals("dateCreated")) {
                        bankAccount.setDateCreated(dateFormat.parse(fields[1]));
                        if (!dateFormat.format(bankAccount.getDateCreated()).equals(fields[1])) throw new IOException("Invalid bank account creation date");
                    } else if (fields[0].equals("isActive")) {
                        if (!fields[1].equals("true") && !fields[1].equals("false")) throw new IOException("Invalid bank account active status");
                        bankAccount.setActive(Boolean.parseBoolean(fields[1]));
                    } else if (fields[0].equals("overDraftFee")) {
                        bankAccount.setOverDraftFee(Double.parseDouble(fields[1]));
                    } else if (fields[0].equals("overDraftCount")) {
                        bankAccount.setOverDraftCount(Integer.parseInt(fields[1]));
                    }
                }
            } catch (ParseException | IllegalArgumentException e) {
                throw new IOException("Invalid bank account property in: " + accountFile, e);
            }
            String[] requiredProperties = {"accountId", "accountName", "type", "balance", "dateCreated", "isActive", "overDraftFee", "overDraftCount"};
            for (String property : requiredProperties) {
                if (!propertiesRead.contains(property)) throw new IOException("Missing bank account property: " + property);
            }
            if (!accountFile.getFileName().toString().equals(bankAccount.getAccountId() + ".properties") || !bankAccount.getAccountName().equals(bankAccountName) || bankAccount.getType() != bankAccountType) {
                throw new IOException("Bank account details do not match the account file: " + accountFile);
            }
            return bankAccount;
        }
        throw new NoSuchFileException("Bank account file not found: " + bankAccountName);
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
        if (userExist(cpr)) return false;
        StringBuilder str = new StringBuilder(cpr).append(",").append(hashedPassword).append(",").append(fullName).append(",").append(role).append("\n");
        Files.writeString(cprsAndAccountsAndCardsFile, str, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return true;
    }

    // the basic functionality is taken from https://www.baeldung.com/java-modify-file-content-based-on-pattern
    public static boolean addBankAccountToCprsAndAccountsAndCardsFile(String cpr, String bankAccountName, BankAccount.Type bankAccountType) throws IOException {
        Path modifiedFile = usersAndAccountDir.resolve("modified.txt");
        String accountPrefix = "#bankAccountName:" + bankAccountName;
        boolean found = false;

        try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile);
             BufferedWriter writer = Files.newBufferedWriter(modifiedFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (!found && fields[0].equals(cpr)) {
                    boolean accountExists = false;
                    for (int i = 4; i < fields.length; i++) {
                        if (fields[i].equals(accountPrefix) || fields[i].startsWith(accountPrefix + "#")) {
                            accountExists = true;
                            break;
                        }
                    }
                    if (accountExists) break;
                    StringBuilder replaced = new StringBuilder(line);
                    replaced.append(",").append("#bankAccountName:").append(bankAccountName); // TODO: dont allow "," or "#" in the name. implement in AppSystem later.
                    replaced.append("#bankAccountType:").append(bankAccountType);
                    line = replaced.toString();
                    found = true;
                }
                writer.write(line);
                writer.newLine();
            }

        }

        if (!found) {
            Files.deleteIfExists(modifiedFile);
            return false;
        }
        Files.move(modifiedFile, cprsAndAccountsAndCardsFile, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    // TODO: maybe combine the two functions later on...
    public static ArrayList<String> getUserCheckingBankAccounts(String cpr) throws IOException {
        ArrayList<String> bankAccounts = new ArrayList<String>();
        try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields[0].equals(cpr)) {
                    // start from 4 cause that's where the bankaccounts start
                    for (int i = 4; i < fields.length; i++) {
                        String[] account = fields[i].split("#");
                        if (account.length > 2 && account[1].startsWith("bankAccountName:") && account[2].equals("bankAccountType:CHECKING")) {
                            bankAccounts.add(account[1].split(":", 2)[1]);
                        }
                    }
                    break;
                }
            }
        }
        return bankAccounts;
    }

    public static ArrayList<String> getUserSavingsBankAccounts(String cpr) throws IOException {
        ArrayList<String> bankAccounts = new ArrayList<String>();
        try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields[0].equals(cpr)) {
                    // start from 4 cause that's where the bankaccounts start
                    for (int i = 4; i < fields.length; i++) {
                        String[] account = fields[i].split("#");
                        if (account.length > 2 && account[1].startsWith("bankAccountName:") && account[2].equals("bankAccountType:SAVINGS")) {
                            bankAccounts.add(account[1].split(":", 2)[1]);
                        }
                    }
                    break;
                }
            }
        }
        return bankAccounts;
    }

    public static boolean cardNumberDoesntExist(long cardNumber) {
        long count = 0; // not int bc/ for some reason it returns long and not int
        try (Stream<String> linesStream = Files.lines(cprsAndAccountsAndCardsFile)) {
            count = linesStream.filter(line -> line.contains(Long.toString(cardNumber))).count();
        } catch (IOException e) {e.printStackTrace();}
        return count == 0;
    }

    public static boolean addCardToCprsAndAccountsAndCardsFile(String cpr, String bankAccountName, long cardNumber, Card.CardTypes cardType, String hashedCode) throws IOException {
        Path modifiedFile = Files.createTempFile(usersAndAccountDir, "cards-", ".tmp");
        String accountPrefix = "#bankAccountName:" + bankAccountName;
        boolean found = false;
        // this would be so slow if it was a real system with many users :(
        try {
            try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile);
                 BufferedWriter writer = Files.newBufferedWriter(modifiedFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    if (!found && fields[0].equals(cpr)) {
                        // start from 4 cause that's where the bankaccounts start
                        for (int i = 4; i < fields.length; i++) {
                            String account = fields[i];
                            if (account.equals(accountPrefix) || account.startsWith(accountPrefix + "#")) {
                                // check if the bank account already has a card associated with it
                                if (account.contains("#cardNumber:")) return false;

                                fields[i] = account + "#cardNumber:" + cardNumber + "#cardType:" + cardType + "#hashedCode:" + hashedCode;
                                line = String.join(",", fields); // reconstruct the line
                                found = true;
                                break;
                            }
                        }
                    }
                    writer.write(line);
                    writer.newLine();
                }
            }
            if (!found) return false;
            Files.move(modifiedFile, cprsAndAccountsAndCardsFile, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } finally {
            Files.deleteIfExists(modifiedFile);
        }
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
            count = linesStream.filter(line -> line.split(",", -1)[0].equals(cpr)).count(); // this is better than .conatins() bc/ it was getting even card numbers as cpr
        }
        return count != 0;
    }

    public static boolean userExist(String cpr) throws IOException{
        long count = 0; // not int bc/ for some reason it returns long and not int
        try (Stream<String> linesStream = Files.lines(cprsAndAccountsAndCardsFile)) {
            count = linesStream.filter(line -> line.split(",", -1)[0].equals(cpr)).count();
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
            setupApplicationDirectories();
//            createUserTransactionsDir("040206343", User.Role.CUSTOMER);
//            createUserTransactionsDir("040206343", User.Role.BANKER);
//            createUserBankAccountTransactionsDir("040206343", User.Role.BANKER , BankAccount.Type.SAVINGS, "saudMain");
//            addUser("040206343", "Saud Salah Al-Ansari Al-Khazriji", User.Role.BANKER);
            BankAccount bankAccount;
            try {
                bankAccount = getBankAccountFromFile("saudMain", "040206343", User.Role.BANKER);
            } catch (NoSuchFileException e) {
                bankAccount = new BankAccount("saudMain", BankAccount.Type.SAVINGS);
            }
            Transaction transaction = new Transaction(50.5, Transaction.TransactionTypes.DEPOSIT, bankAccount.getAccountId(), UUID.randomUUID(), 60, "Successful");
//            transaction.setSuccessful(true);
//            addTransaction("040206343", User.Role.BANKER, bankAccount, transaction);
//            System.out.println(userExist("040206343", User.Role.BANKER));
            addUserToCprsAndAccountsAndCardsFile("040206343","dsakljdas", "Saud Salah", User.Role.BANKER);
            addBankAccountToCprsAndAccountsAndCardsFile("040206343", bankAccount.getAccountName(), bankAccount.getType());
            PlatinumCard card = new PlatinumCard("153153");
            addCardToCprsAndAccountsAndCardsFile("040206343", bankAccount.getAccountName(), card.getCardNumber(), card.getCardType(), card.getHashedCode());


            addOrUpdateBankAccountPropertiesFile(bankAccount, "040206343", User.Role.BANKER);
            BankAccount savedBankAccount = getBankAccountFromFile(bankAccount.getAccountName(), "040206343", User.Role.BANKER);
            System.out.println("Account Id: " + savedBankAccount.getAccountId());
            System.out.println("Account Name: " + savedBankAccount.getAccountName());
            System.out.println("Type: " + savedBankAccount.getType());
            System.out.println("Balance: " + savedBankAccount.getBalance());
            System.out.println("Date Created: " + savedBankAccount.getDateCreated());


            savedBankAccount.setBalance(-50);
            savedBankAccount.setActive(false);
            savedBankAccount.setOverDraftFee(70);
            savedBankAccount.setOverDraftCount(2);
            addOrUpdateBankAccountPropertiesFile(savedBankAccount, "040206343", User.Role.BANKER);
            savedBankAccount = getBankAccountFromFile(savedBankAccount.getAccountName(), "040206343", User.Role.BANKER);
            System.out.println("Updated Balance: " + savedBankAccount.getBalance()); // -50.0
            System.out.println("Active: " + savedBankAccount.isActive()); // false
            System.out.println("Overdraft Fee: " + savedBankAccount.getOverDraftFee()); // 70.0
            System.out.println("Overdraft Count: " + savedBankAccount.getOverDraftCount()); // 2


            BankAccount checkingBankAccount;
            try {
                checkingBankAccount = getBankAccountFromFile("saudChecking", "040206343", User.Role.BANKER);
            } catch (NoSuchFileException e) {
                checkingBankAccount = new BankAccount("saudChecking", BankAccount.Type.CHECKING);
            }
            addBankAccountToCprsAndAccountsAndCardsFile("040206343", checkingBankAccount.getAccountName(), checkingBankAccount.getType());
            addOrUpdateBankAccountPropertiesFile(checkingBankAccount, "040206343", User.Role.BANKER);
            BankAccount savedCheckingBankAccount = getBankAccountFromFile(checkingBankAccount.getAccountName(), "040206343", User.Role.BANKER);
            System.out.println("Account Name: " + savedCheckingBankAccount.getAccountName());
            System.out.println("Type: " + savedCheckingBankAccount.getType());


            System.out.println("Checking Accounts: " + getUserCheckingBankAccounts("040206343"));
            System.out.println("Savings Accounts: " + getUserSavingsBankAccounts("040206343"));
            System.out.println("Missing User Checking Accounts: " + getUserCheckingBankAccounts("000000000"));
            System.out.println("Missing User Savings Accounts: " + getUserSavingsBankAccounts("000000000"));


            System.out.println("Duplicate Account: " + addBankAccountToCprsAndAccountsAndCardsFile("040206343", "saudMain", BankAccount.Type.CHECKING)); // false
        } catch (IOException e) {e.printStackTrace();}
    }
}
