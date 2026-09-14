package Bank.System;

import Bank.Banking.BankAccount;
import Bank.Banking.Transaction;
import Bank.Cards.Card;
import Bank.Cards.MasterCard;
import Bank.Cards.PlatinumCard;
import Bank.Cards.TitaniumCard;
import Bank.Users.Banker;
import Bank.Users.Customer;
import Bank.Users.User;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

//  TODO: finish later, fix the addUserToCprsAndAccountsAndCardsFile first
    public static User getUserFromFile(String cpr) throws IOException{
        User user = null;
        try (Stream<String> lines = Files.lines(cprsAndAccountsAndCardsFile)) {
            String userLine = lines.filter(line -> {
                String[] splitStr = line.split(",", -1);
                return splitStr[0].equals(cpr);
            }).findFirst().orElse(null);
            if (userLine != null) {
                String[] splitStr = userLine.split(",", -1);
                if (splitStr.length < 11) throw new IOException("Incomplete user details: " + cpr); // this line is added by chatGPT
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                User.Role role = User.Role.valueOf(splitStr[3]);
                if (role == User.Role.CUSTOMER) {
                    user = new Customer(splitStr[2], splitStr[4], dateFormat.parse(splitStr[5]), splitStr[0], splitStr[1], splitStr[6], splitStr[7]);
                } else if (role == User.Role.BANKER) {
                    user = new Banker(splitStr[2], splitStr[4], dateFormat.parse(splitStr[5]), splitStr[0], splitStr[1], splitStr[6], splitStr[7]);
                }
                if (!dateFormat.format(user.getDateOfBirth()).equals(splitStr[5])) throw new IOException("Invalid user date of birth"); // edited by chatGPT
                user.setFailedLoginAttempts(Integer.parseInt(splitStr[8]));
                user.setLockoutTimeInMin(Integer.parseInt(splitStr[9]));
                if (!splitStr[10].equals("true") && !splitStr[10].equals("false")) throw new IOException("Invalid user locked out status"); // edited by chatGPT
                user.setLockedOut(Boolean.parseBoolean(splitStr[10]));
            }
        } catch (ParseException | IllegalArgumentException e) {
            throw new IOException("Invalid user details: " + cpr, e);
        }
        return user;
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

    public static boolean addUserToCprsAndAccountsAndCardsFile(User user) throws IOException {
        if (userExist(user.getCpr())) return false;
        String[] userDetails = {user.getCpr(), user.getHashedPassword(), user.getfName(), user.getlName(), user.getSecurityQuestion(), user.getHashedSecurityQuestionAnswer()};
        for (String detail : userDetails) {
            if (detail == null || detail.contains(",") || detail.contains("#") || detail.contains("\n") || detail.contains("\r")) throw new IOException("User details cannot be null or contain commas, # or line breaks");
        }
        if (user.getDateOfBirth() == null || user.getRole() == null) throw new IOException("User date of birth and role are required");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder str = new StringBuilder(user.getCpr());
        str.append(",").append(user.getHashedPassword());
        str.append(",").append(user.getfName());
        str.append(",").append(user.getRole());
        str.append(",").append(user.getlName());
        str.append(",").append(dateFormat.format(user.getDateOfBirth()));
        str.append(",").append(user.getSecurityQuestion());
        str.append(",").append(user.getHashedSecurityQuestionAnswer());
        str.append(",").append(user.getFailedLoginAttempts());
        str.append(",").append(user.getLockoutTimeInMin());
        str.append(",").append(user.isLockedOut());
        str.append("\n");
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
                        if (!fields[i].startsWith("#bankAccountName:")) continue;
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
                        if (!fields[i].startsWith("#bankAccountName:")) continue;
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

    public static Card getCardFromFile(long cardNumber) throws IOException {
        Card card = null;
        String cardNumSearch = "#cardNumber:" + cardNumber + "#";
        try (Stream<String> lines = Files.lines(cprsAndAccountsAndCardsFile)) {
            String userLine = lines.filter(line -> line.contains(cardNumSearch)).findFirst().orElse(null);
            if (userLine != null) {
                // explanation: split into two halfs and discard the first half since the second half includes the info we need
                // then split again to get rid whats after we need and take the first half
                // then split one last time to split the card variable
                String[] fields = userLine.split(cardNumSearch, 2)[1].split(",", 2)[0].split("#", -1);
                if (fields.length != 2 || !fields[0].startsWith("cardType:") || !fields[1].startsWith("hashedCode:")) throw new IOException("Incomplete card details: " + cardNumber);
                Card.CardTypes cardType = Card.CardTypes.valueOf(fields[0].split(":", 2)[1]);
                String hashedCode = fields[1].split(":", 2)[1];
                if (cardType == Card.CardTypes.MASTERCARD) {
                    card = new MasterCard(hashedCode);
                } else if (cardType == Card.CardTypes.PLATINUMCARD) {
                    card = new PlatinumCard(hashedCode);
                } else if (cardType == Card.CardTypes.TITANIUMCARD) {
                    card = new TitaniumCard(hashedCode);
                }
                card.setCardNumber(cardNumber);
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid card details: " + cardNumber, e);
        }
        return card;
    }

    public static String getUserCprFromCardNumberFromFile(String cardNumber) throws IOException {
        String cpr = null;
        String cardNumSearch = "#cardNumber:" + cardNumber + "#";
        try (Stream<String> lines = Files.lines(cprsAndAccountsAndCardsFile)) {
            String userLine = lines.filter(line -> line.contains(cardNumSearch)).findFirst().orElse(null);
            if (userLine != null) {
                cpr = userLine.split(",", 2)[0];
            }
        }
        return cpr;
    }

    public static Card getBankAccountCardFromFile(String cpr, String bankAccountName) throws IOException {
        Card card = null;
        String accountPrefix = "#bankAccountName:" + bankAccountName;
        try (Stream<String> lines = Files.lines(cprsAndAccountsAndCardsFile)) {
            String userLine = lines.filter(line -> {
                String[] splitStr = line.split(",", -1);
                return splitStr[0].equals(cpr);
            }).findFirst().orElse(null);
            if (userLine != null) {
                String[] fields = userLine.split(",", -1);
                for (int i = 11; i < fields.length; i++) {
                    if (fields[i].equals(accountPrefix) || fields[i].startsWith(accountPrefix + "#")) {
                        String[] account = fields[i].split("#", -1);
                        if (account.length == 3 && account[2].startsWith("bankAccountType:")) return null;
                        if (account.length != 6 || !account[2].startsWith("bankAccountType:") || !account[3].startsWith("cardNumber:") || !account[4].startsWith("cardType:") || !account[5].startsWith("hashedCode:")) throw new IOException("Incomplete card details: " + bankAccountName);
                        long cardNumber = Long.parseLong(account[3].split(":", 2)[1]);
                        Card.CardTypes cardType = Card.CardTypes.valueOf(account[4].split(":", 2)[1]);
                        String hashedCode = account[5].split(":", 2)[1];
                        if (cardType == Card.CardTypes.MASTERCARD) {
                            card = new MasterCard(hashedCode);
                        } else if (cardType == Card.CardTypes.PLATINUMCARD) {
                            card = new PlatinumCard(hashedCode);
                        } else if (cardType == Card.CardTypes.TITANIUMCARD) {
                            card = new TitaniumCard(hashedCode);
                        }
                        card.setCardNumber(cardNumber);
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid card details: " + bankAccountName, e);
        }
        return card;
    }

    public static boolean cardNumberDoesntExist(long cardNumber) {
        long count = 0; // not int bc/ for some reason it returns long and not int
        try (Stream<String> linesStream = Files.lines(cprsAndAccountsAndCardsFile)) {
            count = linesStream.filter(line -> {
                String[] fields = line.split(",", -1);
                for (int i = 4; i < fields.length; i++) {
                    if (!fields[i].startsWith("#bankAccountName:")) continue;
                    String[] account = fields[i].split("#");
                    for (String property : account) {
                        if (property.equals("cardNumber:" + cardNumber)) return true;
                    }
                }
                return false;
            }).count();
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

    public static ArrayList<Transaction> getTransactionsFromFile(String cpr, String bankAccountName) throws IOException {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        User user = getUserFromFile(cpr);
        if (user == null) return transactions;
        BankAccount bankAccount = getBankAccountFromFile(bankAccountName, cpr, user.getRole());
        Path relativeBase = null;
        if (user.getRole() == User.Role.CUSTOMER) {
            relativeBase = customersTransactionsDir.resolve(cpr);
        } else if (user.getRole() == User.Role.BANKER) {
            relativeBase = bankersTransactionsDir.resolve(cpr);
        }
        relativeBase = relativeBase.resolve(bankAccount.getType().toString().toLowerCase()).resolve(bankAccountName);

        Path[] transactionFiles;
        try (Stream<Path> filesStream = Files.walk(relativeBase, 3)) {
            transactionFiles = filesStream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().equals("transactions.txt")).toArray(Path[]::new);
        }
        for (Path transactionFile : transactionFiles) {
            Transaction transaction = null;
            ArrayList<String> propertiesRead = new ArrayList<String>();
            try (BufferedReader reader = Files.newBufferedReader(transactionFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("############")) {
                        if (transaction != null) {
                            if (propertiesRead.size() != 11) throw new IOException("Incomplete transaction details in: " + transactionFile);
                            transactions.add(transaction);
                        }
                        transaction = new Transaction(0, null, null, null, 0, null);
                        propertiesRead.clear();
                        continue;
                    }
                    String[] fields = line.split(": ", 2);
                    if (transaction == null || fields.length != 2) throw new IOException("Incomplete transaction property: " + line);
                    if (propertiesRead.contains(fields[0])) throw new IOException("Duplicate transaction property: " + fields[0]);
                    propertiesRead.add(fields[0]);
                    if (fields[0].equals("Transaction Id")) {
                        transaction.setTransactionId(UUID.fromString(fields[1]));
                    } else if (fields[0].equals("Transfer Id")) {
                        transaction.setTransferId(fields[1].equals("null") ? null : UUID.fromString(fields[1]));
                    } else if (fields[0].equals("amount")) {
                        transaction.setAmount(Double.parseDouble(fields[1]));
                    } else if (fields[0].equals("date")) {
                        transaction.setDate(LocalDate.parse(fields[1]));
                    } else if (fields[0].equals("time")) {
                        transaction.setTime(LocalTime.parse(fields[1]));
                    } else if (fields[0].equals("transaction type")) {
                        transaction.setType(Transaction.TransactionTypes.valueOf(fields[1]));
                    } else if (fields[0].equals("from")) {
                        transaction.setFromAccountId(fields[1].equals("null") ? null : UUID.fromString(fields[1]));
                    } else if (fields[0].equals("to")) {
                        transaction.setToAccountId(fields[1].equals("null") ? null : UUID.fromString(fields[1]));
                    } else if (fields[0].equals("balance")) {
                        transaction.setPostTransactionBalance(Double.parseDouble(fields[1]));
                    } else if (fields[0].equals("is successful")) {
                        if (!fields[1].equals("true") && !fields[1].equals("false")) throw new IOException("Invalid transaction successful status");
                        transaction.setSuccessful(Boolean.parseBoolean(fields[1]));
                    } else if (fields[0].equals("note")) {
                        transaction.setNote(fields[1]);
                    } else {
                        throw new IOException("Invalid transaction property: " + fields[0]);
                    }
                }
            } catch (DateTimeParseException | IllegalArgumentException e) {
                throw new IOException("Invalid transaction details in: " + transactionFile, e);
            }
            if (transaction != null) {
                if (propertiesRead.size() != 11) throw new IOException("Incomplete transaction details in: " + transactionFile);
                transactions.add(transaction);
            }
        }
        transactions.sort((transaction1, transaction2) -> {
            int dateComparison = transaction1.getDate().compareTo(transaction2.getDate());
            if (dateComparison == 0) return transaction1.getTime().compareTo(transaction2.getTime());
            return dateComparison;
        });
        return transactions;
    }

    public static ArrayList<Transaction> getTransactionsFromFile(String cpr, String bankAccountName, LocalDate startDate, LocalDate endDate) throws IOException {
        if (startDate == null || endDate == null) throw new IOException("Start date and end date are required");
        if (startDate.isAfter(endDate)) throw new IOException("Start date cannot be after end date");
        ArrayList<Transaction> transactions = getTransactionsFromFile(cpr, bankAccountName);
        ArrayList<Transaction> filteredTransactions = new ArrayList<Transaction>();
        for (Transaction transaction : transactions) {
            if (!transaction.getDate().isBefore(startDate) && !transaction.getDate().isAfter(endDate)) {
                filteredTransactions.add(transaction);
            }
        }
        return filteredTransactions;
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Banker user = new Banker("Saud", "Salah", dateFormat.parse("2004-02-06"), "040206343", AppSystem.hash("123456"), "Demo number 9999999999999999?", "demoHashedAnswer");
            user.setFailedLoginAttempts(3);
            user.setLockoutTimeInMin(5);
            user.setLockedOut(true);
            System.out.println("User Added: " + addUserToCprsAndAccountsAndCardsFile(user));
            System.out.println("Duplicate User: " + addUserToCprsAndAccountsAndCardsFile(user));
            System.out.println("User Exists With Leading Zero: " + userExist(user.getCpr()));
            System.out.println("User Exists Without Leading Zero: " + userExist("40206343"));

            try (BufferedReader reader = Files.newBufferedReader(cprsAndAccountsAndCardsFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    if (fields[0].equals(user.getCpr())) {
                        if (fields.length < 11 || fields[4].startsWith("#bankAccountName:")) {
                            System.out.println("Existing user row does not contain all User fields");
                            break;
                        }
                        SimpleDateFormat savedDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        System.out.println("Saved CPR Matches: " + fields[0].equals(user.getCpr()));
                        System.out.println("Saved Hashed Password Matches: " + fields[1].equals(user.getHashedPassword()));
                        System.out.println("Saved First Name Matches: " + fields[2].equals(user.getfName()));
                        System.out.println("Saved Role Matches: " + fields[3].equals(user.getRole().toString()));
                        System.out.println("Saved Last Name Matches: " + fields[4].equals(user.getlName()));
                        System.out.println("Saved Date Of Birth Matches: " + savedDateFormat.parse(fields[5]).equals(user.getDateOfBirth()));
                        System.out.println("Saved Security Question Matches: " + fields[6].equals(user.getSecurityQuestion()));
                        System.out.println("Saved Hashed Security Question Answer Matches: " + fields[7].equals(user.getHashedSecurityQuestionAnswer()));
                        System.out.println("Saved Failed Login Attempts Match: " + (Integer.parseInt(fields[8]) == user.getFailedLoginAttempts()));
                        System.out.println("Saved Lockout Time Matches: " + (Integer.parseInt(fields[9]) == user.getLockoutTimeInMin()));
                        System.out.println("Saved Locked Out Status Matches: " + (Boolean.parseBoolean(fields[10]) == user.isLockedOut()));
                        break;
                    }
                }
            }
            System.out.println("Security Question Number Is Available As Card Number: " + cardNumberDoesntExist(9999999999999999L));

            BankAccount bankAccount;
            try {
                bankAccount = getBankAccountFromFile("saudMain", user.getCpr(), user.getRole());
            } catch (NoSuchFileException e) {
                bankAccount = new BankAccount("saudMain", BankAccount.Type.SAVINGS);
            }
            Transaction transaction = new Transaction(50.5, Transaction.TransactionTypes.DEPOSIT, bankAccount.getAccountId(), UUID.randomUUID(), 60, "Successful");
//            transaction.setSuccessful(true);
//            addTransaction("040206343", User.Role.BANKER, bankAccount, transaction);
//            System.out.println(userExist("040206343", User.Role.BANKER));
            System.out.println("Savings Account Added: " + addBankAccountToCprsAndAccountsAndCardsFile(user.getCpr(), bankAccount.getAccountName(), bankAccount.getType()));
            PlatinumCard card = new PlatinumCard("153153");
            boolean cardAdded = addCardToCprsAndAccountsAndCardsFile(user.getCpr(), bankAccount.getAccountName(), card.getCardNumber(), card.getCardType(), card.getHashedCode());
            System.out.println("Card Added: " + cardAdded);
            if (cardAdded) System.out.println("Added Card Number Is Available: " + cardNumberDoesntExist(card.getCardNumber()));
            System.out.println("Duplicate Card: " + addCardToCprsAndAccountsAndCardsFile(user.getCpr(), bankAccount.getAccountName(), card.getCardNumber(), card.getCardType(), card.getHashedCode()));


            addOrUpdateBankAccountPropertiesFile(bankAccount, user.getCpr(), user.getRole());
            BankAccount savedBankAccount = getBankAccountFromFile(bankAccount.getAccountName(), user.getCpr(), user.getRole());
            System.out.println("Account Id: " + savedBankAccount.getAccountId());
            System.out.println("Account Name: " + savedBankAccount.getAccountName());
            System.out.println("Type: " + savedBankAccount.getType());
            System.out.println("Balance: " + savedBankAccount.getBalance());
            System.out.println("Date Created: " + savedBankAccount.getDateCreated());


            savedBankAccount.setBalance(-50);
            savedBankAccount.setActive(false);
            savedBankAccount.setOverDraftFee(70);
            savedBankAccount.setOverDraftCount(2);
            addOrUpdateBankAccountPropertiesFile(savedBankAccount, user.getCpr(), user.getRole());
            savedBankAccount = getBankAccountFromFile(savedBankAccount.getAccountName(), user.getCpr(), user.getRole());
            System.out.println("Updated Balance: " + savedBankAccount.getBalance()); // -50.0
            System.out.println("Active: " + savedBankAccount.isActive()); // false
            System.out.println("Overdraft Fee: " + savedBankAccount.getOverDraftFee()); // 70.0
            System.out.println("Overdraft Count: " + savedBankAccount.getOverDraftCount()); // 2


            BankAccount checkingBankAccount;
            try {
                checkingBankAccount = getBankAccountFromFile("saudChecking", user.getCpr(), user.getRole());
            } catch (NoSuchFileException e) {
                checkingBankAccount = new BankAccount("saudChecking", BankAccount.Type.CHECKING);
            }
            System.out.println("Checking Account Added: " + addBankAccountToCprsAndAccountsAndCardsFile(user.getCpr(), checkingBankAccount.getAccountName(), checkingBankAccount.getType()));
            addOrUpdateBankAccountPropertiesFile(checkingBankAccount, user.getCpr(), user.getRole());
            BankAccount savedCheckingBankAccount = getBankAccountFromFile(checkingBankAccount.getAccountName(), user.getCpr(), user.getRole());
            System.out.println("Account Name: " + savedCheckingBankAccount.getAccountName());
            System.out.println("Type: " + savedCheckingBankAccount.getType());


            System.out.println("Checking Accounts: " + getUserCheckingBankAccounts(user.getCpr()));
            System.out.println("Savings Accounts: " + getUserSavingsBankAccounts(user.getCpr()));
//            System.out.println("Missing User Checking Accounts: " + getUserCheckingBankAccounts("000000000"));
//            System.out.println("Missing User Savings Accounts: " + getUserSavingsBankAccounts("000000000"));


            System.out.println("Duplicate Account: " + addBankAccountToCprsAndAccountsAndCardsFile(user.getCpr(), "saudMain", BankAccount.Type.CHECKING)); // false
//            User user2 = getUserFromFile("040206343");
//            System.out.println(user2.getCpr()+ user2.getfName());
            ArrayList<Transaction> transactionArrayList = getTransactionsFromFile("040206343", "saudMain"); //TODO: aparently its case sentitive, fix later
//            System.out.println(transactionArrayList.get(0));

            System.out.println("\n\n\n\n");
            try {
                String password = "123456";
                System.out.println(AppSystem.checkPasswordMatch("040206343", password));
                System.out.println(AppSystem.hash("123456"));
            } catch (Exception e) {e.printStackTrace();}

        } catch (IOException | ParseException e) {e.printStackTrace();}
    }
}
