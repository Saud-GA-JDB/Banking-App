package Bank.System;
import Bank.Bank;
import Bank.Banking.BankAccount;
import Bank.Users.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// state manager so like a controller in mvc
public class AppSystem {
    private Bank bank;
    private boolean isLoggedIn;
    private User user;
    private Screen.Page currentPage;
    BankAccount currentBankAccount;

    public AppSystem (String bankName) {
        isLoggedIn = false;
        bank = new Bank(bankName);
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




    public static void main(String[] args) {
//        System.out.println(hash("saud"));
//        System.out.println(hash("saud"));
//        System.out.println(hash("suad"));
    }
}
