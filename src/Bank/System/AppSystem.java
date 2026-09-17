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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// state manager so like a controller in mvc
public class AppSystem {
    private Bank bank;
    private boolean isLoggedIn;
    private User user;
    private Screen.Page currentPage;
    BankAccount currentBankAccount;
    private Screen screen;

    // TODO: putting an ArrayList<BankAccount> currentUserBankAccounts would help from always retrieving them

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
//                System.out.println("not yet implemented but your cpr and password are correct!");
                setUserSession(cpr);
                user.setFailedLoginAttempts(0); // reset
                FileDatabaseSystem.updateUserInFile(user);
                flag = false;
                if(getUser().getRole() == User.Role.BANKER) loadBankerDashBoard();
                else loadCustomerDashBoardPage();
                return;
            } else {
                // user is locked
                System.out.println("Sorry, You are locked out. Please try again after 1 minute");
                TimeUnit.SECONDS.sleep(3);
            }
        }
    }

    public void loadBankerDashBoard() throws Exception{
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            Screen.clearConsole();
            int input = screen.bankerDashBoardPage(bank);
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
                case 2: // customer services
                    Screen.clearConsole();
                    screen.customerServicesPage(bank);
                    setCurrentPage(Screen.Page.CUSTOMERSERVICES);
//                    flag = false;
                    break;
                case 3: // my banking
//                    System.out.println("coming soon!");
//                    TimeUnit.SECONDS.sleep(3);
                    loadCustomerDashBoardPage();
                    flag = false;
                    break;
                default:
                    System.out.println("Invalid Choice"); // maybe add a note section to the pages
                    TimeUnit.SECONDS.sleep(3);
                    Screen.clearConsole();
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
                    if (!loadChooseAccountPage()) return; // choose account first
                    loadWithdrawPage();
                    setCurrentPage(Screen.Page.WITHDRAW);
                    flag = false;
                    break;
                case 4:
                    loadTransferChoicesPage();
                    setCurrentPage(Screen.Page.TRANSFERCHOICES);
                    flag = false;
                    break;
                case 5:
                    loadBalanceAndStatementsPage();
                    setCurrentPage(Screen.Page.BALANCEANDSTATEMENTS);
                    flag = false;
                    break;
                case 6:
//                    loadAccountsAndCardsPage(); TODO: implement
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

    public void loadBalanceAndStatementsPage() throws Exception {
        if (!loadChooseAccountPage()) return;
        boolean flag = true;
        while(flag) {
            Screen.clearConsole();
            int input = screen.balanceAndStatementsPage(bank);
            switch (input) {
                case 0: // back
                    loadCustomerDashBoardPage();
                    break;
                case 1: // view Balance
                    loadBalancePage();
                    break;
                case 2: // view Detailed account statement
                    loadStatementPage();
                    break;
                case 3: // filter Transactions
                    loadFilterTransactionsPage();
                    break;
                default:
                    System.out.println("Invalid selection. Please choose from the list.");
                    TimeUnit.SECONDS.sleep(3);
            }
        }
    }

    public void loadFilterTransactionsPage() throws InterruptedException, IOException {
        boolean flag = true;
        while (flag) {
            Screen.clearConsole();

            int input = screen.filterTransactionsPage(bank);
            LocalDate today = LocalDate.now();
            LocalDate startDate;
            LocalDate endDate = today;

            switch (input) {
                case 0:
                    return;
                case 1:
                    startDate = today;
                    break;
                case 2:
                    startDate = today.minusDays(1);
                    endDate = startDate;
                    break;
                case 3:
                    // Previous calendar week, Monday through Sunday.
                    endDate = today.minusDays(today.getDayOfWeek().getValue());
                    startDate = endDate.minusDays(6);
                    break;
                case 4:
                    startDate = today.minusDays(6);
                    break;
                case 5:
                    endDate = today.withDayOfMonth(1).minusDays(1);
                    startDate = endDate.withDayOfMonth(1);
                    break;
                case 6:
                    startDate = today.minusDays(29);
                    break;
                case 7:
                    Screen.clearConsole();
                    String[] dates = screen.customDateTimePage(bank);
                    try {
                        startDate = LocalDate.parse(dates[0]);
                        endDate = LocalDate.parse(dates[1]);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date. Please use yyyy-MM-dd.");
                        TimeUnit.SECONDS.sleep(3);
                        continue;
                    }
                    if (startDate.isAfter(endDate)) {
                        System.out.println("Start date cannot be after end date.");
                        TimeUnit.SECONDS.sleep(3);
                        continue;
                    }
                    break;
                default:
                    System.out.println("Invalid selection. Please choose from the list.");
                    TimeUnit.SECONDS.sleep(3);
                    continue;
            }
            Screen.clearConsole();
            screen.statementPage(bank, getCurrentBankAccount(), FileDatabaseSystem.getTransactionsFromFile(getUser().getCpr(), getCurrentBankAccount().getAccountName(), startDate, endDate));
        }
    }

    public void loadStatementPage() throws IOException {
        Screen.clearConsole();
        ArrayList<Transaction> transactions = FileDatabaseSystem.getTransactionsFromFile(getUser().getCpr(), getCurrentBankAccount().getAccountName());
        screen.statementPage(bank, getCurrentBankAccount(), transactions);
    }

    public void loadBalancePage() {
        Screen.clearConsole();
        screen.balancePage(bank, getCurrentBankAccount());
    }

    public void loadTransferChoicesPage() throws Exception {
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            int input = screen.transferChoicesPage(bank);
            switch (input) {
                case 0: //back
                    loadCustomerDashBoardPage();
                    setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                    flag = false;
                    break;
                case 1: //transfer to own accounts
                    Screen.clearConsole();
                    System.out.println("select the from account");
                    TimeUnit.SECONDS.sleep(3);
                    if (!loadChooseAccountPage()) return;
                    loadTransferToOwnAccountPage();
                    flag = false;
                    break;
                case 2: // transfer to others accounts
                    Screen.clearConsole();
                    System.out.println("select the from account");
                    TimeUnit.SECONDS.sleep(3);
                    if (!loadChooseAccountPage()) return;
                    loadTransferPage();
                    flag = false;
                    break;
                default:
                    System.out.println("Invalid Choice");
                    TimeUnit.SECONDS.sleep(3);
                    Screen.clearConsole();
            }
        }
    }

    public void loadTransferPage() throws Exception{
        Screen.clearConsole();
        Card fromCard = FileDatabaseSystem.getBankAccountCardFromFile(getUser().getCpr(), getCurrentBankAccount().getAccountName());
        if (fromCard == null) {
            System.out.println("there is no card associated with " + getCurrentBankAccount().getAccountName());
            System.out.println("You'll be redirected to the dashboard shortly.");
            TimeUnit.SECONDS.sleep(3);
            loadCustomerDashBoardPage();
            return;
        }

        String receiverCpr = null;
        boolean flag = true;
        while (flag) {
            receiverCpr = screen.otherAccountPage(bank);
            if (receiverCpr.equalsIgnoreCase("back")) {
                loadCustomerDashBoardPage();
                return;
            }
            if (receiverCpr.equalsIgnoreCase(getUser().getCpr())) {
                System.out.println("You cant enter your own CPR");
                TimeUnit.SECONDS.sleep(3);
                loadCustomerDashBoardPage();
                return;
            }
            // user does exist
            if (FileDatabaseSystem.userExist(receiverCpr)) flag = false;
            else {
                System.out.println("Sorry. This cpr doesnt exist or is not registered with us");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
            }
        }

        // user does exist
        ArrayList<BankAccount> receiverBankAccounts = FileDatabaseSystem.getUserBankAccountsFromFile(receiverCpr);
        int index = 0;
        flag = true;
        while (flag) {
            Screen.clearConsole();
            int input = screen.chooseAccountPage(bank, receiverBankAccounts);

            if (input == 0) {// back
                setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                loadCustomerDashBoardPage();
                return;
            }

            if (input < 0 || input > receiverBankAccounts.size()) { // invalid choice
                System.out.println("Invalid choice");
                TimeUnit.SECONDS.sleep(3);
            } else { // valid
                index = input - 1;
                flag = false;
            }
        }

        // user chose to account

        BankAccount receiverBankAccount = receiverBankAccounts.get(index);
        Transaction transaction = new Transaction(0.0, Transaction.TransactionTypes.TRANSFER, getCurrentBankAccount().getAccountId(), receiverBankAccount.getAccountId(), 0.0, null);
        Transaction fromTransaction = null;
        Transaction toTransaction = null;

        flag = true;
        while (flag) {
            Screen.clearConsole();
            double amount = screen.transferPage(bank, getCurrentBankAccount(), receiverBankAccount);

            if (amount == 0.0) {
                System.out.println("You'll be redirected to the dashboard shortly.");
                TimeUnit.SECONDS.sleep(3);
                loadCustomerDashBoardPage();
                return;
            }

            if (amount > 0.0) {
                transaction.setAmount(amount);
                loadDailyTransferAmounts(fromCard, getCurrentBankAccount());
                fromTransaction = getCurrentBankAccount().makeTransaction(fromCard, transaction);
                toTransaction = receiverBankAccount.receiveTransaction(fromTransaction);
                flag = false;
            } else {
                System.out.println("Invalid. please enter a valid amount.");
                TimeUnit.SECONDS.sleep(3);
            }
        }

        // transactions are done with with success or not
        User receiverUser = FileDatabaseSystem.getUserFromFile(receiverCpr);

        if (!fromTransaction.isSuccessful() || !toTransaction.isSuccessful()) {
            fromTransaction.setSuccessful(false);
            toTransaction.setSuccessful(false);
            // revert back
            fromTransaction.setPostTransactionBalance(getCurrentBankAccount().getBalance() + transaction.getAmount());
            toTransaction.setPostTransactionBalance(receiverBankAccount.getBalance() - transaction.getAmount());
        } else { // both success -> save
            FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(getCurrentBankAccount(), getUser().getCpr(), getUser().getRole());
            FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(receiverBankAccount, receiverCpr, receiverUser.getRole());
        }
        // save transactions
        FileDatabaseSystem.addTransaction(getUser().getCpr(), getUser().getRole(), getCurrentBankAccount(), fromTransaction);
        FileDatabaseSystem.addTransaction(receiverCpr, receiverUser.getRole(), receiverBankAccount, toTransaction);

        loadTransactionResultsPage(fromTransaction);

    }

    public void loadTransferToOwnAccountPage() throws Exception{
        Screen.clearConsole();
        // yes we could use lambda but this is more readable, at least to me :)
        ArrayList<BankAccount> userBankAccounts = FileDatabaseSystem.getUserBankAccountsFromFile(getUser().getCpr()).stream().filter(bankAccount -> {
                                    return !getCurrentBankAccount().getAccountName().equalsIgnoreCase(bankAccount.getAccountName());
                        }).collect(Collectors.toCollection(ArrayList::new));
        int toBankAccountIndex = 0;

        boolean flag = true;
        while (flag) {
            int input = screen.chooseAccountPage(bank, userBankAccounts);
            if (input==0) { //user chose back
                setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                loadCustomerDashBoardPage();
                return;
            }
            if (input > userBankAccounts.size() || input < 0) { // invalid input
                Screen.clearConsole();
                System.out.println("Invalid choice.");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
            } else { // user chose an account
                toBankAccountIndex = input - 1;
                flag = false;
            }
        }
        // user now chose from and to bank accounts
        BankAccount toBankAccount = userBankAccounts.get(toBankAccountIndex);
        Screen.clearConsole();

        Card fromCard = FileDatabaseSystem.getBankAccountCardFromFile(getUser().getCpr(), getCurrentBankAccount().getAccountName());
        if (fromCard == null) {
            System.out.println("Sorry, there is no card linked with " + getCurrentBankAccount().getAccountName());
            System.out.println("You'll be redirected to the dashboard shortly.");
            TimeUnit.SECONDS.sleep(3);
            loadCustomerDashBoardPage();
            return;
        }
//        Card toCard = FileDatabaseSystem.getBankAccountCardFromFile(getUser().getCpr(), getCurrentBankAccount().getAccountName());
        Transaction transaction = new Transaction(0.0, Transaction.TransactionTypes.TRANSFEROWN, getCurrentBankAccount().getAccountId(), null, 0.0, null);
        flag = true;
        while (flag) {
            double amount = screen.transferPage(bank, getCurrentBankAccount(), toBankAccount);
            if (amount == 0.0) {
                setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                loadCustomerDashBoardPage();
                return;
            }
            if (amount > 0.0) { // valid amount
//                Transaction transaction = new Transaction(amount, Transaction.TransactionTypes.TRANSFEROWN, getCurrentBankAccount().getAccountId(), null, 0.0, null);
//                getCurrentBankAccount().makeTransaction(fromCard, transaction);
                transaction.setAmount(amount);
//                loadTransactionResultsPage(transaction);
                flag = false;
            } else {
                System.out.println("Invalid amount. Please select a valid amount.");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
            }
        }
        // user entered a valid amount
        loadDailyTransferAmounts(fromCard, getCurrentBankAccount());
        Transaction fromTransaction = getCurrentBankAccount().makeTransaction(fromCard, transaction);
        Transaction toTransaction = toBankAccount.receiveTransaction(fromTransaction);
        if (!fromTransaction.isSuccessful() || !toTransaction.isSuccessful()) { // any failed
            fromTransaction.setSuccessful(false);
            toTransaction.setSuccessful(false);
            // rejected transfers leave account balances unchanged
            fromTransaction.setPostTransactionBalance(getCurrentBankAccount().getBalance());
            toTransaction.setPostTransactionBalance(toBankAccount.getBalance());
        } else { // both success -> save
            FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(getCurrentBankAccount(), getUser().getCpr(), getUser().getRole());
            FileDatabaseSystem.addOrUpdateBankAccountPropertiesFile(toBankAccount, getUser().getCpr(), getUser().getRole());
        }
        // save transactions
        FileDatabaseSystem.addTransaction(getUser().getCpr(), getUser().getRole(), getCurrentBankAccount(), fromTransaction);
        FileDatabaseSystem.addTransaction(getUser().getCpr(), getUser().getRole(), toBankAccount, toTransaction);

        loadTransactionResultsPage(fromTransaction);
    }

    public void loadWithdrawPage() throws Exception{
        Screen.clearConsole();
        Card card = FileDatabaseSystem.getBankAccountCardFromFile(getUser().getCpr(), getCurrentBankAccount().getAccountName());
        if (card == null) {
            System.out.println("Sorry. This bank Account doesn't have any card associated with it.");
            System.out.println("\n\nYou will be redirected to the dashboard shortly.");
            TimeUnit.SECONDS.sleep(3);
            loadCustomerDashBoardPage();
            return;
        }

        boolean flag = true;
        while (flag) {
            double input = screen.withdrawPage(bank, getCurrentBankAccount());
            if (input == 0.0) {
                loadCustomerDashBoardPage();
                return;
            }
            if (input > 0.0) { // valid amount
                Transaction transaction = new Transaction(input, Transaction.TransactionTypes.WITHDRAW, getCurrentBankAccount().getAccountId(), null, 0.0, null);
                getCurrentBankAccount().makeTransaction(card, transaction);
                loadTransactionResultsPage(transaction);
                return;
            } else {
                System.out.println("Invalid amount. Please select a valid amount.");
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
                    if (!loadChooseAccountPage()) return; //choose account first
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
            } else {
                System.out.println("Invalid amount. Please select a valid amount.");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
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
                System.out.println("Invalid amount. Please select a valid amount.");
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
                                receiverTransaction.setSuccessful(false);
                                receiverTransaction.setNote("over limit");
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

    public boolean loadChooseAccountPage() throws Exception{
        Screen.clearConsole();
        setCurrentBankAccount(null);
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
                return false;
            }
            int input = screen.chooseAccountPage(bank, bankAccountArrayList);
            if (input==0) { //user chose back
                setCurrentPage(Screen.Page.CUSTOMERDASHBOARD);
                loadCustomerDashBoardPage();
                return false;
            }
            if (input > bankAccountArrayList.size() || input < 0) { // invalid input
                Screen.clearConsole();
                System.out.println("Invalid choice.");
                TimeUnit.SECONDS.sleep(3);
                Screen.clearConsole();
            } else { // user chose an account
                setCurrentBankAccount(bankAccountArrayList.get(input-1));
                setCurrentPage(Screen.Page.CHOOSEACCOUNT);
                flag = false;
            }

        }
        return true;
    }

    /*
    ====================================================================
    Helper Methods
    ====================================================================
     */

    public void loadDailyTransferAmounts(Card card, BankAccount bankAccount) throws IOException {
        card.setAmountTransferredToday(0);
        card.setAmountTransferredToOwnAccountToday(0);
        LocalDate today = LocalDate.now();
        ArrayList<Transaction> transactions = FileDatabaseSystem.getTransactionsFromFile(getUser().getCpr(), bankAccount.getAccountName(), today, today);
        for (Transaction transaction : transactions) {
            if (!transaction.isSuccessful() || !bankAccount.getAccountId().equals(transaction.getFromAccountId())) continue;
            if (transaction.getType() == Transaction.TransactionTypes.TRANSFER) {
                card.setAmountTransferredToday(card.getAmountTransferredToday() + transaction.getAmount());
            } else if (transaction.getType() == Transaction.TransactionTypes.TRANSFEROWN) {
                card.setAmountTransferredToOwnAccountToday(card.getAmountTransferredToOwnAccountToday() + transaction.getAmount());
            }
        }
    }

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

            if ( !fromTransaction.isSuccessful() || !toTransation.isSuccessful()) { // if any not success set both to failed and dont update bankAccount
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
