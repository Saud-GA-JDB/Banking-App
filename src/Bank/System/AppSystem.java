package Bank.System;
import Bank.Bank;
import Bank.Banking.BankAccount;
import Bank.Cards.Card;
import Bank.Users.Customer;
import Bank.Users.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
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
                incrementUserFailedLoginAttempts(cpr);
            } else if (!checkIsUserLocked(cpr)){ // sign in
                // TODO: continue with uer dashboard weather customer or banker dash...
                // also, check weather the user is lockedout or not before allowing him to continue
                System.out.println("not yet implemented but your cpr and password are correct!");
                setUserSession(cpr);
                user.setFailedLoginAttempts(0); // reset
                flag = false;
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
                //apply the penalty
                incrementUserFailedLoginAttempts(cardNumber);
            } else {
                // TODO: continue with uer dashboard weather customer or banker dash...
                // also, check weather the user is lockedout or not before allowing him to continue
                String cpr = FileDatabaseSystem.getUserCprFromCardNumberFromFile(cardNumber);
                if (!checkIsUserLocked(cpr)) {
                    System.out.println("not yet implemented but your card number and passcode are correct!");
                    setUserSession(cpr);
                    user.setFailedLoginAttempts(0); // reset
                    flag = false;
                } else { // user is locked out
                    System.out.println("Sorry, You are locked out. Please try again after 1 minute");
                    TimeUnit.SECONDS.sleep(3);
                }
            }
        }
    }

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
    // TODO: im here...............................................................!!!!!!!!!!!!!!
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
                    loadDepositToOwnAccountPage();
                    setCurrentPage(Screen.Page.DEPOSIT);
                    flag = false;
                    break;
                case 2: // deposit to another account
//                    loadDepositPage(); TODO: implement
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
    // TODO: continue here... select account first before depositing
    public void loadDepositToOwnAccountPage() throws Exception{
        Screen.clearConsole();
        loadChooseAccountPage();

        // TODO: Continue here//////////////////////////////////////////////////

    }

    public void loadChooseAccountPage() throws Exception{
        Screen.clearConsole();
        boolean flag = true;
        while (flag) {
            ArrayList<BankAccount> bankAccountArrayList = FileDatabaseSystem.getUserBankAccountsFromFile(user.getCpr());
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
                break;
            } else { // user chose an account
                setCurrentBankAccount(bankAccountArrayList.get(input+1));
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

    public void setUserSession(String cpr) throws Exception{
        setUser(FileDatabaseSystem.getUserFromFile(cpr));
        setLoggedIn(true);
    }

    public void incrementUserFailedLoginAttempts(String cprOrCardNumber) throws Exception{
        if (cprOrCardNumber.length() == 16)
            cprOrCardNumber = FileDatabaseSystem.getUserCprFromCardNumberFromFile(cprOrCardNumber);
//        if (FileDatabaseSystem.userExist(cprOrCardNumber)) {} test without first since the method already handles such cases
        User user = FileDatabaseSystem.getUserFromFile(cprOrCardNumber);
        System.out.println("before: " + user.getFailedLoginAttempts());

        if (user.getFailedLoginAttempts() < 3) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts()+1);
            FileDatabaseSystem.addUserToCprsAndAccountsAndCardsFile(user); // TODO: create a method that updates user info in file instead of this!
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
        return card.getHashedCode().equals(hash(passcode));
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
