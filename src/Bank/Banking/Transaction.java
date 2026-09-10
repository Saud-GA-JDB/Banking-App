package Bank.Banking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Transaction {
    private UUID transactionId;
    private UUID transferId;
    private double amount;
    private LocalDate date;
    private LocalTime time;
    public enum TransactionTypes {WITHDRAW, TRANSFER, DEPOSIT}
    private TransactionTypes type;
    private UUID fromAccountId;
    private UUID toAccountId;
    private double postTransactionBalance;
    private boolean isSuccessful;
    private String note;

    public Transaction(double amount, TransactionTypes type, UUID fromAccountId, UUID toAccountId, double postTransactionBalance, String note) {
        transactionId = UUID.randomUUID();
        transferId = UUID.randomUUID();
        this.amount = amount;
        date = LocalDate.now();
        time = LocalTime.now();
        this.type = type;
        this.toAccountId = toAccountId;
        this.fromAccountId = fromAccountId;
        this.postTransactionBalance = postTransactionBalance;
        this.note = note;
    }

    /*
    =============================================================================
    Setters and getters
    =============================================================================
     */

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public TransactionTypes getType() {
        return type;
    }

    public void setType(TransactionTypes type) {
        this.type = type;
    }

    public UUID getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(UUID toAccountId) {
        this.toAccountId = toAccountId;
    }

    public double getPostTransactionBalance() {
        return postTransactionBalance;
    }

    public void setPostTransactionBalance(double postTransactionBalance) {
        this.postTransactionBalance = postTransactionBalance;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public UUID getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(UUID fromAccountId) {
        this.fromAccountId = fromAccountId;
    }
/*
    =============================================================================
    Methods
    =============================================================================
     */
}
