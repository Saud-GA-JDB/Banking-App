package Bank.System;
import Bank.Bank;
import Bank.Banking.BankAccount;
import Bank.Banking.Transaction;
import Bank.Cards.Card;
import Bank.Users.Customer;
import Bank.Users.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

// state manager so like a controller in mvc
public class AppSystem {
    private Bank bank;
    private boolean isLoggedIn;
    private User user;
    private Screen.Page currentPage;
    BankAccount currentBankAccount;
    private Screen screen;

    HashMap<String, LocalTime> lockedUserAnsTimeMap; // store cpr and time user was locked

    public AppSystem (String bankName) {
        isLoggedIn = false;
        bank = new Bank(bankName);
        screen = new Screen();
        lockedUserAnsTimeMap = new HashMap<>();
    }



    /*
    =============================================================================
    Setters and getters
    =============================================================================
     */

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BankAccount getCurrentBankAccount() {
        return currentBankAccount;
    }

    public void setCurrentBankAccount(BankAccount currentBankAccount) {
        this.currentBankAccount = currentBankAccount;
    }

    public Screen.Page getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Screen.Page currentPage) {
        this.currentPage = currentPage;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    /*
    =============================================================================
    Methods
    =============================================================================
     */

    public void resetAppState() {
        setLoggedIn(false);
        setUser(null);
        setCurrentBankAccount(null);
        setCurrentPage(Screen.Page.START);
    }

    /*
    -----------------------------------------------------------------
    SHA3 Encryption Algorithm Taken From:
    https://www.baeldung.com/sha-256-hashing-java
     */
    public static String hash(String originalString) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            final byte[] hashbytes = digest.digest(
                    originalString.getBytes(StandardCharsets.UTF_8));
            String sha3Hex = bytesToHex(hashbytes);
            return sha3Hex;
        } catch (NoSuchAlgorithmException e) { e.printStackTrace();}
        return "";
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    /*
    -----------------------------------------------------------------
     */

    /*
    this is a draft to see if this approach works or not ----------------------
     */


    public void loadStartPage() throws Exception {
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            int input = screen.startPage(bank);
            switch (input) {
                case 1:
                    setCurrentPage(Screen.Page.LOGINCHOICES);
                    flag = false;
                    loadLoginChoicesPage();
                    break;
                default:
                    System.out.println("Invalid Choice"); // maybe add a note section to the pages
                    TimeUnit.SECONDS.sleep(3);
                    Screen.clearConsole();
            }
        }
    }
    public void loadLoginChoicesPage() throws Exception {
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            int input = screen.loginChoicesPage(bank);
            switch (input) {
                case 0:
                    loadStartPage();
                    setCurrentPage(Screen.Page.START);
                    flag = false;
                    break;
                case 1:
                    loadLoginWithCprPage();
                    setCurrentPage(Screen.Page.LOGINWITHCPR);
                    flag = false;
                    break;
                case 2:
                    loadLoginWithCardPage();
                    setCurrentPage(Screen.Page.LOGINWITHCARD);
                    flag = false;
                    break;
                case 3:
                    loadOpenAccountPage();
                    setCurrentPage(Screen.Page.OPENACCOUNT);
                    flag = false;
                    break;
                default:
                    System.out.println("Invalid Choice"); // maybe add a note section to the pages
                    TimeUnit.SECONDS.sleep(3);
                    Screen.clearConsole();
            }
        }
    }

    public void loadLoginWithCprPage() throws Exception {
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            String[] input = screen.loginWithCpr(bank);

            if (input.length==1) { // mean the user inputted back
                flag = false;
                loadLoginChoicesPage();
                return;
            }

            String cpr = input[0];
            String password = input[1];

            if (!FileDatabaseSystem.userExist(cpr) || !checkPasswordMatch(cpr, password)) {
                System.out.println("CPR or Password are wrong...");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
                //apply the penalty
                incrementUserFailedLoginAttempts(cpr); // already saves in file
            } else if (!checkIsUserLocked(cpr)){ // sign in
                // TODO: continue with uer dashboard weather customer or banker dash...
                // also, check weather the user is lockedout or not before allowing him to continue
                System.out.println("not yet implemented but your cpr and password are correct!");
                setUserSession(cpr);
                user.setFailedLoginAttempts(0); // reset
                FileDatabaseSystem.updateUserInFile(user);
                flag = false;
                loadCustomerDashBoardPage();
                return;
            } else {
                // user is locked
                System.out.println("Sorry, You are locked out. Please try again after 1 minute");
                TimeUnit.SECONDS.sleep(3);
            }
        }
    }

    public void loadLoginWithCardPage() throws Exception{
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            String[] input = screen.loginWithCardPage(bank);

            if (input.length==1 && input[0].equals("back")) { // mean the user inputted back
                flag = false;
                loadLoginChoicesPage();
                return;
            }

            String cardNumber = input[0];
            String passcode = input[1];



            if (!checkPasscodeMatch(cardNumber, passcode)) {
                System.out.println("card number or Passcode are wrong...");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
                //apply the penalty if card does exist only
                incrementUserFailedLoginAttempts(cardNumber); // if user exist it updates

            } else {
                // TODO: continue with uer dashboard weather customer or banker dash...
                // also, check weather the user is lockedout or not before allowing him to continue
                String cpr = FileDatabaseSystem.getUserCprFromCardNumberFromFile(cardNumber);
                if (!checkIsUserLocked(cpr)) {
                    System.out.println("not yet implemented but your card number and passcode are correct!");
                    setUserSession(cpr); // set user
                    user.setFailedLoginAttempts(0); // reset
                    FileDatabaseSystem.updateUserInFile(user); // save
                    flag = false;
                    loadCustomerDashBoardPage();
                    return;
                } else { // user is locked out
                    System.out.println("Sorry, You are locked out. Please try again after 1 minute");
                    TimeUnit.SECONDS.sleep(3);
                }
            }
        }
    }

    // TODO: TEST again later
    public void loadOpenAccountPage()  throws Exception{
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            String[] input = screen.addCustomerPage(bank);

            if (input.length==1 && input[0].equals("back")) { // mean the user inputted back
                flag = false;
                loadLoginChoicesPage();
                return;
            }

            String fName = input[0];
            String lName = input[1];
            String dateOfBirth = input[2];
            // TODO: handle later id it throws Exception
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            LocalDate dateWithFormatter = LocalDate.parse(dateOfBirth, formatter);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            LocalDate dateWithFormatter = LocalDate.parse(dateOfBirth, dateFormat.parse());

            String cpr = input[3];
            String password = input[4];
            String hashedPassword = hash(password);
            String securityQuestion = input[5];
            String hashedsecurityQuestionAnswer = hash(input[6]);
            /*
            Input validation
             */
            boolean valid = true;
            if (cpr.length() != 9) {
                System.out.println("Invalid CPR...");
                valid = false;
            }
            if (FileDatabaseSystem.userExist(cpr)) {
                System.out.println("CPR already exists.");
                valid = false;
            }
            if (password.length() < 8) {
                System.out.println("Password must be at least 8 characters long.");
                valid = false;
            }

            if (valid) {
                User user1 = new Customer(fName, lName, dateFormat.parse(dateOfBirth), cpr, hashedPassword, securityQuestion, hashedsecurityQuestionAnswer);
                FileDatabaseSystem.addUserToCprsAndAccountsAndCardsFile(user1);

                Screen.clearConsole();
                System.out.println("Account Created.");
                System.out.println("You'll be redirected to the sign in page");
                TimeUnit.SECONDS.sleep(3);
                loadLoginWithCprPage();
                flag = false;
                return;
            }
            TimeUnit.SECONDS.sleep(3);

        }
    }

    public void loadCustomerDashBoardPage() throws Exception{
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            int input = screen.customerDashBoardPage(bank);
            switch (input) {
                case 1: // logout
                    setUser(null);
                    isLoggedIn = false;
                    Screen.clearConsole();
                    System.out.println("logging out...");
                    TimeUnit.SECONDS.sleep(3);
                    System.out.println("logged out!");
                    TimeUnit.SECONDS.sleep(1);
                    loadStartPage();
                    setCurrentPage(Screen.Page.START);
                    flag = false;
                    break;
                case 2: // deposit
//                    loadChooseAccountPage();//choose account first
                    loadDepositChoicesPage();
                    setCurrentPage(Screen.Page.DEPOSITCHOICES);
                    flag = false;
                    break;
                case 3:
//                    loadWithdrawPage(); TODO: implement
                    setCurrentPage(Screen.Page.WITHDRAW);
                    flag = false;
                    break;
                case 4:
//                    loadTransferChoicesPage(); TODO: implement
                    setCurrentPage(Screen.Page.TRANSFERCHOICES);
                    flag = false;
                    break;
                case 5:
//                    loadBalanceAndStatementsPage(); TODO: implement
                    setCurrentPage(Screen.Page.BALANCEANDSTATEMENTS);
                    flag = false;
                    break;
                case 6:
//                    loadBalanceAndStatementsPage(); TODO: implement
                    setCurrentPage(Screen.Page.ACCOUNTSANDCARDS);
                    flag = false;
                    break;
                default:
                    System.out.println("Invalid Choice"); // maybe add a note section to the pages
                    TimeUnit.SECONDS.sleep(3);
                    Screen.clearConsole();
            }
        }
    }
    public void loadDepositChoicesPage() throws Exception {
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            int input = screen.depositChoicesPage(bank);
            switch (input) {
                case 0: // back
                    loadCustomerDashBoardPage();
                    setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                    flag = false;
                    break;
                case 1: // deposit to own account
                    loadChooseAccountPage(); //choose account first
                    loadDepositToOwnAccountPage();
                    setCurrentPage(Screen.Page.DEPOSIT);
                    flag = false;
                    break;
                case 2: // deposit to another account
                    loadDepositPage();
                    setCurrentPage(Screen.Page.DEPOSIT);
                    flag = false;
                    break;
                default:
                    System.out.println("Invalid Choice"); // maybe add a note section to the pages
                    TimeUnit.SECONDS.sleep(3);
                    Screen.clearConsole();
            }
        }
    }

    public void loadDepositToOwnAccountPage() throws Exception{
        Screen.clearConsole();
//        loadChooseAccountPage();

        // TODO: Continue here//////////////////////////////////////////////////
        // this is wrong. i need to choose to account first.
        while (true) {

            // here i got an error bc/ getCurrentBankAccount() returned null but it the whole method shouldnt be called if the choice is not a bank account
            double input = screen.depositToOwnAccountPage(getBank(), getCurrentBankAccount());
            if (input == 0.0) { // back
                setCurrentBankAccount(null);
                setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                loadCustomerDashBoardPage();
                return;
            } else if (input > 0.0) { //valid amount
                Transaction transaction = makeTransactionAndcheckCardAssociatedWithBankAccountAndLimits(getCurrentBankAccount(), null, Transaction.TransactionTypes.DEPOSITOWN, input, null);
                loadTransactionResultsPage(transaction);
                return;
            }
        }

    }

    // this method is a mess, it needs cleaning up but theres no time
    // spent one whole day just working on this method :(
    public void loadDepositPage() throws Exception {
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            ArrayList<String> input = screen.depositPage(bank);
            if (input.size()==0 || input.isEmpty()) {
                loadCustomerDashBoardPage();
                return;
            }

            double amount = Double.parseDouble(input.get(1)); /*String toBankAccountId = input.get(1);*/
            String receiverCpr = input.get(0);

            ArrayList<BankAccount> receiverBankAccounts = null;
            int index = 0;
            if (amount < 0.0) {
                System.out.println("cant deposit negative amount!");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
            } else if (!FileDatabaseSystem.userExist(receiverCpr)) { // cpr doesnt exist
                System.out.println("this user is not registered with us");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
            } else {
                receiverBankAccounts = FileDatabaseSystem.getUserBankAccountsFromFile(receiverCpr);
                if (receiverBankAccounts == null || receiverBankAccounts.size() == 0) {
                    System.out.println("receiver doesn't have any bank accounts");
                    Screen.clearConsole();
                } else { // valid bank accounts TODO: this block should be a separate method!
                    boolean innerFlag = true;
                    while (innerFlag) {
                        int input2 = screen.chooseAccountPage(bank, receiverBankAccounts);

                        if (input2 < 0 || input2 > receiverBankAccounts.size()) {
                            System.out.println("invalid choice ");
                        } else if (input2 != 0) { // valid choice
                            index = input2 - 1;
                            BankAccount receiverBankAccount = receiverBankAccounts.get(index);
                            // TODO: needs fixing for deposit bc/ currently money would be withdrawn from bank account and deposited to bank account, but deposit is cash!
//                            Transaction transaction = makeTransactionAndcheckCardAssociatedWithBankAccountAndLimits(getCurrentBankAccount(), receiverBankAccounts.get(index), Transaction.TransactionTypes.DEPOSIT, amount, receiverCpr);
                            Transaction depositerTransaction = new Transaction(amount, Transaction.TransactionTypes.DEPOSIT, null, receiverBankAccount.getAccountId(), 0.0, "Cash Deposit");
                            depositerTransaction.setSuccessful(true);
                            Transaction receiverTransaction = receiverBankAccount.receiveTransaction(depositerTransaction);
                            User receiverUser = FileDatabaseSystem.getUserFromFile(receiverCpr);
                            Card receiverCard = FileDatabaseSystem.getBankAccountCardFromFile(receiverCpr, receiverBankAccount.getAccountName());

                            if (receiverCard == null) {
                                receiverTransaction.setSuccessful(false);
                                receiverTransaction.setNote("No Card");
                                depositerTransaction.setSuccessful(false);
                                depositerTransaction.setNote("No Card");
                                receiverTransaction.setPostTransactionBalance(receiverBankAccount.getBalance() - amount); //revert post balance
                            } else if (!receiverTransaction.isSuccessful() || !BankAccount.isTransactionUnderDailyLimit(receiverCard, Transaction.TransactionTypes.DEPOSIT, amount)) {
                                depositerTransaction.setSuccessful(false);
                                depositerTransaction.setNote("over limit");
                                receiverTransaction.setPostTransactionBalance(receiverBankAccount.getBalance() - amount); //revert post balance
                            } else { // success
                                FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(receiverBankAccount, receiverCpr, receiverUser.getRole());
                                receiverCard.setAmountDepositedToday(receiverCard.getAmountDepositedToday() + amount);
                                // TODO: need a way to save into file like a card.properties file for future retrieval
                            }
                            FileDatabaseSystem.addTransaction(receiverCpr, receiverUser.getRole(), receiverBankAccount, receiverTransaction);

                            loadTransactionResultsPage(depositerTransaction);
                            return;
                        } else { // user input is 0. back
                            innerFlag = false;
                        }
                    }
                }
            }


        }

    }

    public void loadTransactionResultsPage(Transaction transaction) throws Exception{
        // Conttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt
        screen.transactionResultPage(bank, transaction);
        loadCustomerDashBoardPage();
    }

    public void loadChooseAccountPage() throws Exception{
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            ArrayList<BankAccount> bankAccountArrayList = FileDatabaseSystem.getUserBankAccountsFromFile(user.getCpr());
//            bankAccountArrayList.forEach(System.out::println);
            if (bankAccountArrayList.size()==0) { // no bank accounts
                System.out.println("You do not have any Bank Accounts. Please open one to proceed with this action.");
                System.out.println("You'll be redirected to the dashboard.");
                TimeUnit.SECONDS.sleep(3);
                setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                loadCustomerDashBoardPage();
                return;
            }
            int input = screen.chooseAccountPage(bank, bankAccountArrayList);
            if (input==0) { //user chose back
                setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                loadCustomerDashBoardPage();
                return;
            }
            if (input > bankAccountArrayList.size() || input < 0) { // invalid input
                Screen.clearConsole();
                System.out.println("Invalid choice.");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
            } else { // user chose an account
                setCurrentBankAccount(bankAccountArrayList.get(input-1));
                setCurrentPage(Screen.Page.DEPOSIT);
                flag = false;
            }

        }
    }

    /*
    ====================================================================
    Helper Methods
    ====================================================================
     */

    // toBankAccount and toCpr can be null if its withdraw or deposit to own
    // TODO: if there is a to bankAccount check that it exist before calling this method. IMPORTANT!!!
    public Transaction makeTransactionAndcheckCardAssociatedWithBankAccountAndLimits(BankAccount fromBankAccount, BankAccount toBankAccount, Transaction.TransactionTypes transactionType, double amount, String toCpr) throws Exception {
        Card card = FileDatabaseSystem.getBankAccountCardFromFile(user.getCpr(), fromBankAccount.getAccountName());
        if (card == null) { // bank account doesnt have a card
//            Screen.clearConsole();
            System.out.println("No card is associated with this bank account. Choose another bank account or open a card");
//            System.out.println("You'll be redirected to the dashboard.");
            TimeUnit.SECONDS.sleep(3);
//            setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
//            loadCustomerDashBoardPage();
            return new Transaction(amount, transactionType, fromBankAccount.getAccountId(), null, 0.0, "no Card associated with bank account");
        }

        Transaction fromTransaction = new Transaction(amount, transactionType, fromBankAccount.getAccountId(), null, 0.0, null);
        if (toBankAccount != null)
            fromTransaction.setToAccountId(toBankAccount.getAccountId());

        fromTransaction = fromBankAccount.makeTransaction(card, fromTransaction);

        // TODO: Continue././././././././././


        // there is to bankAccount
        if (toBankAccount != null) {
            Transaction toTransation = toBankAccount.receiveTransaction(fromTransaction);

            User receivingUser = FileDatabaseSystem.getUserFromFile(toCpr);

            if ( !fromTransaction.isSuccessful() && !toTransation.isSuccessful()) { // if any not success set both to failed and dont update bankAccount
                fromTransaction.setSuccessful(false);
                toTransation.setSuccessful(false);
            } else { // both success
//                fromTransaction.setSuccessful(false);
//                toTransation.setSuccessful(false);
//                FileDatabaseSystem.addTransaction(user.getCpr(), user.getRole(), fromBankAccount, fromTransaction);
//                FileDatabaseSystem.addTransaction(receivingUser.getCpr(), receivingUser.getRole(), toBankAccount, toTransation);
                FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(fromBankAccount, user.getCpr(), user.getRole());
                FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(toBankAccount, receivingUser.getCpr(), receivingUser.getRole());
            }
            //save transactions
            FileDatabaseSystem.addTransaction(receivingUser.getCpr(), receivingUser.getRole(), toBankAccount, toTransation);
        } else if (fromTransaction.isSuccessful()){ // transfer to own account and success -> update else dont
            FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(fromBankAccount, user.getCpr(), user.getRole());
        }
        FileDatabaseSystem.addTransaction(user.getCpr(), user.getRole(), fromBankAccount, fromTransaction);

//        switch (transactionType) {
//            case DEPOSITOWN:
//
//                break;
//            case DEPOSIT:
//                toBankAccount.receiveTransaction(fromTransaction)
//                break;
//            case TRANSFER:
//
//                break;
//            case TRANSFEROWN:
//
//                break;
//            case WITHDRAW:
//
//                break;
//        }
        // amount is good to go!
        return fromTransaction;
    }

    public void setUserSession(String cpr) throws Exception{
        setUser(FileDatabaseSystem.getUserFromFile(cpr));
        setLoggedIn(true);
    }

    public void incrementUserFailedLoginAttempts(String cprOrCardNumber) throws Exception{
        if (cprOrCardNumber.length() == 16)
            cprOrCardNumber = FileDatabaseSystem.getUserCprFromCardNumberFromFile(cprOrCardNumber);
//        if (FileDatabaseSystem.userExist(cprOrCardNumber)) {} test without first since the method already handles such cases
        User user = FileDatabaseSystem.getUserFromFile(cprOrCardNumber);
        if (user == null) return;
        System.out.println("before: " + user.getFailedLoginAttempts());

        if (user.getFailedLoginAttempts() < 3) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts()+1);
            FileDatabaseSystem.updateUserInFile(user); // TODO: create a method that updates user info in file instead of this!
        }
        ///
        System.out.println(user.getFailedLoginAttempts()); // remove
        ///

        if (user.getFailedLoginAttempts() >= 3) {
            lockedUserAnsTimeMap.put(cprOrCardNumber, LocalTime.now());
            System.out.println("entered the blackout list!"); // remove
        }
    }

    public boolean checkIsUserLocked(String cpr) {
        LocalTime timeUserWasLocked = lockedUserAnsTimeMap.get(cpr);

        if (timeUserWasLocked == null) return false;

        LocalTime currentTime = LocalTime.now();
        if (Duration.between(timeUserWasLocked, currentTime).toMinutes() >= 1) {
            lockedUserAnsTimeMap.remove(cpr);
            return false;
        }
        return true;
    }

    public static boolean checkPasswordMatch (String cpr, String password) throws Exception {
        User user = FileDatabaseSystem.getUserFromFile(cpr);
        return user.getHashedPassword().equals(hash(password));
    }

    public static boolean checkPasscodeMatch (String cardNumber, String passcode) throws Exception {
        Card card = FileDatabaseSystem.getCardFromFile(cardNumber);
        return card.getHashedCode().equals(hash(passcode)); //already handles null
    }


    /*
    end of draft approach ------------------------------------------------------
     */




    public static void main(String[] args) {
//        System.out.println(hash("saud"));
//        System.out.println(hash("saud"));
//        System.out.println(hash("suad"));
        try {
            AppSystem sys = new AppSystem("NBB");
            sys.loadStartPage();
        } catch (Exception e) {e.printStackTrace();}
    }
}
