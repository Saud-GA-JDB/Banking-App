package Bank.System;
import Bank.Bank;
import Bank.Banking.BankAccount;
import Bank.Cards.Card;
import Bank.Users.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

// state manager so like a controller in mvc
public class AppSystem {
    private Bank bank;
    private boolean isLoggedIn;
    private User user;
    private Screen.Page currentPage;
    BankAccount currentBankAccount;
    private Screen screen;

    public AppSystem (String bankName) {
        isLoggedIn = false;
        bank = new Bank(bankName);
        screen = new Screen();
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
//                    loadLoginWithCardPage(); // TODO: implement...
                    setCurrentPage(Screen.Page.LOGINWITHCARD);
                    flag = false;
                    break;
                case 3:
//                    loadOpenAccountPage(); // TODO: implement...
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
            } else {
                // TODO: continue with uer dashboard weather customer or banker dash...
                System.out.println("not yet implemented but your cpr and password are correct!");
                flag = false;
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

            if ()
        }
    }

    public static boolean checkPasswordMatch (String cpr, String password) throws Exception {
        User user = FileDatabaseSystem.getUserFromFile(cpr);
        return user.getHashedPassword().equals(hash(password));
    }

    public static boolean checkPasscodeMatch (String cardNumber, String passcode) throws Exception {
        Card card = FileDatabaseSystem.getCardFromFile()
        return user.getHashedPassword().equals(hash(password));
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
