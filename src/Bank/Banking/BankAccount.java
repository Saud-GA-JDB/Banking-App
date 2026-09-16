package Bank.Banking;

import Bank.Cards.Card;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // TODO: withdrawFromOwn(), ....

    public static boolean isTransactionUnderDailyLimit(Card card, Transaction.TransactionTypes transactionType, double amount) {
        double limit = 0.0;
        double usedfromLimit = 0.0;
        switch (transactionType) {
            case DEPOSITOWN:
                limit = card.getDepositLimit();
                usedfromLimit = card.getAmountDepositedToOwnAccountToday();
                break;
            case DEPOSIT:
                limit = card.getDepositLimit();
                usedfromLimit = card.getAmountDepositedToday();
                break;
            case TRANSFER:
                limit = card.getTransferLimit();
                usedfromLimit = card.getAmountTransferredToday();
                break;
            case TRANSFEROWN:
                limit = card.getTransferLimitOwnAccount();
                usedfromLimit = card.getAmountTransferredToOwnAccountToday();
                break;
            case WITHDRAW:
                limit = card.getWithdrawLimit();
                usedfromLimit = card.getAmountWithdrawnToday();
                break;
        }
        if (amount + usedfromLimit > limit) // over limit
            return false;
        return true;
    }

    // int t is a temp quick fix. 0=from, 1=to
    public Transaction makeTransaction(Card card, Transaction transaction) {


        double amount = transaction.getAmount();
        Transaction.TransactionTypes transactionType = transaction.getType();

        transaction.setDate(LocalDate.now());
        transaction.setTime(LocalTime.now());

        if (!isActive() && !(transactionType == Transaction.TransactionTypes.DEPOSIT || transactionType == Transaction.TransactionTypes.DEPOSITOWN )) {
            transaction.setSuccessful(false);
            transaction.setNote("Account is deactivated. Deposit first to reactivate.");
            return transaction;
        }

        if (!isTransactionUnderDailyLimit(card, transactionType, amount)) {
            transaction.setSuccessful(false);
            transaction.setNote("Transaction is over daily limit");
            return transaction;
        }

        switch (transactionType) {
            case DEPOSITOWN, DEPOSIT:
                setBalance(getBalance()+amount);
                if (getBalance() >= 0.0) {
                    setActive(true); setOverDraftCount(0);
                }
                transaction.setSuccessful(true);
                transaction.setPostTransactionBalance(getBalance());
                break;
            case TRANSFER, TRANSFEROWN:
                if (amount > getBalance()) {
                    transaction.setSuccessful(false);
                    transaction.setNote("insufficient funds");
                    transaction.setPostTransactionBalance(getBalance());
                } else { // successful
                    setBalance(getBalance() - amount);
                    if (transactionType == Transaction.TransactionTypes.TRANSFER) {
                        card.setAmountTransferredToday(card.getAmountTransferredToday() + amount);
                    } else {
                        card.setAmountTransferredToOwnAccountToday(card.getAmountTransferredToOwnAccountToday() + amount);
                    }
                    transaction.setSuccessful(true);
                    transaction.setPostTransactionBalance(getBalance());
                }
                break;
            case WITHDRAW:
                if (getBalance() < 0.0 && amount > 100) { // reject transaction bc/ balance is neg and withdrawing > 100
                    transaction.setSuccessful(false);
                    transaction.setNote("can't withdraw more than 100 when you're balance is negative");
                    transaction.setPostTransactionBalance(getBalance());
                } else {
                    setBalance(getBalance() - amount);
                    if (getBalance() < 0.0) {
                        setBalance(getBalance() - getOverDraftFee());
                        setOverDraftCount(getOverDraftCount() + 1);
                        transaction.setNote("overdraft fee of "+ getOverDraftFee()+" charged");
                    }
                    transaction.setPostTransactionBalance(getBalance());
                    transaction.setSuccessful(true);
                }
                if (getOverDraftCount() >= 2) setActive(false);
                break;
        }
        return transaction;
    }

    public Transaction receiveTransaction(Transaction transaction) {
        double amount = transaction.getAmount();
        Transaction.TransactionTypes transactionType = transaction.getType();
        Transaction receiverTransaction = new Transaction(amount, transactionType, transaction.getFromAccountId(), transaction.getToAccountId(), getBalance(), transaction.getNote());
        receiverTransaction.setTransferId(transaction.getTransferId());
        receiverTransaction.setDate(transaction.getDate());
        receiverTransaction.setTime(transaction.getTime());

        if (!transaction.isSuccessful()) return receiverTransaction;
        // dont know why theres a switch statement, i just copied it from the other method
        switch (transactionType) {
            case TRANSFER, TRANSFEROWN, DEPOSIT, DEPOSITOWN:
                setBalance(getBalance()+amount);
                if (getBalance() >= 0.0) {
                    setActive(true); setOverDraftCount(0);
                }
                receiverTransaction.setSuccessful(true);
                receiverTransaction.setPostTransactionBalance(getBalance());
                break;
        }
        return receiverTransaction;
    }

}
