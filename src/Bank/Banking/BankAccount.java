package Bank.Banking;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class BankAccount {
    private UUID accountId;
    private String accountName;
    public enum Type {CHECKING, SAVINGS}
    Type type;
    private double balance;
    private Date dateCreated;
    private boolean isActive;
    private double overDraftFee;
    private int overDraftCount;

    public BankAccount(String accountName, Type type) {
        accountId = UUID.randomUUID();
        this.accountName = accountName;
        balance = 0;
        isActive = true;
        overDraftFee = 35;
        overDraftCount = 0;
        dateCreated = Date.from(Instant.now());
        this.type = type;
    }

    /*
    =============================================================================
    Setters and getters
    =============================================================================
     */

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getOverDraftFee() {
        return overDraftFee;
    }

    public void setOverDraftFee(double overDraftFee) {
        this.overDraftFee = overDraftFee;
    }

    public int getOverDraftCount() {
        return overDraftCount;
    }

    public void setOverDraftCount(int overDraftCount) {
        this.overDraftCount = overDraftCount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /*
    =============================================================================
    methods
    =============================================================================
     */
}
