package Bank.Banking;

import Bank.Cards.PlatinumCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BankAccountTest {
    BankAccount bankAccount;
    BankAccount otherAccount;
    PlatinumCard card;

    @BeforeEach
    public void setUp() {
        bankAccount = new BankAccount("Savings", BankAccount.Type.SAVINGS);
        otherAccount = new BankAccount("Checking", BankAccount.Type.CHECKING);
        card = new PlatinumCard("passcodeHash");
        bankAccount.setBalance(500);
    }

    @Test
    @DisplayName("When a new account is created then it is active with zero balance")
    public final void whenNewAccountIsCreatedThenItIsActiveWithZeroBalance() {
        Assertions.assertTrue(otherAccount.isActive());
        Assertions.assertEquals(0, otherAccount.getBalance(), 0.001);
        Assertions.assertNotEquals(bankAccount.getAccountId(), otherAccount.getAccountId());
    }

    @Test
    @DisplayName("When money is deposited to own account then balance increases")
    public final void whenMoneyIsDepositedToOwnAccountThenBalanceIncreases() {
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.DEPOSITOWN, null, bankAccount.getAccountId(), 500, "");
        bankAccount.makeTransaction(card, transaction);
        Assertions.assertTrue(transaction.isSuccessful());
        Assertions.assertEquals(600, bankAccount.getBalance(), 0.001);
    }

    @Test
    @DisplayName("When money is deposited to another account then its balance increases")
    public final void whenMoneyIsDepositedToAnotherAccountThenItsBalanceIncreases() {
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.DEPOSIT, null, otherAccount.getAccountId(), 0, "");
        otherAccount.makeTransaction(card, transaction);
        Assertions.assertTrue(transaction.isSuccessful());
        Assertions.assertEquals(100, otherAccount.getBalance(), 0.001);
        Assertions.assertEquals(500, bankAccount.getBalance(), 0.001);
    }

    @Test
    @DisplayName("When money is withdrawn then balance decreases")
    public final void whenMoneyIsWithdrawnThenBalanceDecreases() {
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, 500, "");
        bankAccount.makeTransaction(card, transaction);
        Assertions.assertTrue(transaction.isSuccessful());
        Assertions.assertEquals(400, bankAccount.getBalance(), 0.001);
    }

    @Test
    @DisplayName("When money is transferred then both account balances change")
    public final void whenMoneyIsTransferredThenBothAccountBalancesChange() {
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.TRANSFER, bankAccount.getAccountId(), otherAccount.getAccountId(), 500, "");
        bankAccount.makeTransaction(card, transaction);
        Transaction receivedTransaction = otherAccount.receiveTransaction(transaction);
        Assertions.assertTrue(transaction.isSuccessful());
        Assertions.assertTrue(receivedTransaction.isSuccessful());
        Assertions.assertEquals(400, bankAccount.getBalance(), 0.001);
        Assertions.assertEquals(100, otherAccount.getBalance(), 0.001);
        Assertions.assertEquals(transaction.getTransferId(), receivedTransaction.getTransferId());
    }

    @Test
    @DisplayName("When transfer exceeds balance then neither account balance changes")
    public final void whenTransferExceedsBalanceThenNeitherAccountBalanceChanges() {
        Transaction transaction = new Transaction(600, Transaction.TransactionTypes.TRANSFER, bankAccount.getAccountId(), otherAccount.getAccountId(), 500, "");
        bankAccount.makeTransaction(card, transaction);
        Transaction receivedTransaction = otherAccount.receiveTransaction(transaction);
        Assertions.assertFalse(transaction.isSuccessful());
        Assertions.assertFalse(receivedTransaction.isSuccessful());
        Assertions.assertEquals(500, bankAccount.getBalance(), 0.001);
        Assertions.assertEquals(0, otherAccount.getBalance(), 0.001);
    }

    @Test
    @DisplayName("When withdrawal causes an overdraft then a fee of 35 is charged")
    public final void whenWithdrawalCausesOverdraftThenFeeOf35IsCharged() {
        Transaction transaction = new Transaction(550, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, 500, "");
        bankAccount.makeTransaction(card, transaction);
        Assertions.assertTrue(transaction.isSuccessful());
        Assertions.assertEquals(-85, bankAccount.getBalance(), 0.001);
        Assertions.assertEquals(1, bankAccount.getOverDraftCount());
    }

    @Test
    @DisplayName("When two overdrafts occur then the account is deactivated")
    public final void whenTwoOverdraftsOccurThenAccountIsDeactivated() {
        bankAccount.makeTransaction(card, new Transaction(550, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, 500, ""));
        bankAccount.makeTransaction(card, new Transaction(50, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, -85, ""));
        Assertions.assertEquals(2, bankAccount.getOverDraftCount());
        Assertions.assertFalse(bankAccount.isActive());
    }

    @Test
    @DisplayName("When a deposit resolves negative balance then the account is reactivated")
    public final void whenDepositResolvesNegativeBalanceThenAccountIsReactivated() {
        bankAccount.setBalance(-100);
        bankAccount.setActive(false);
        bankAccount.setOverDraftCount(2);
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.DEPOSITOWN, null, bankAccount.getAccountId(), -100, "");
        bankAccount.makeTransaction(card, transaction);
        Assertions.assertTrue(transaction.isSuccessful());
        Assertions.assertTrue(bankAccount.isActive());
        Assertions.assertEquals(0, bankAccount.getBalance(), 0.001);
        Assertions.assertEquals(0, bankAccount.getOverDraftCount());
    }

    @Test
    @DisplayName("When balance is negative then withdrawal over 100 is rejected")
    public final void whenBalanceIsNegativeThenWithdrawalOver100IsRejected() {
        bankAccount.setBalance(-50);
        Transaction transaction = new Transaction(101, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, -50, "");
        bankAccount.makeTransaction(card, transaction);
        Assertions.assertFalse(transaction.isSuccessful());
        Assertions.assertEquals(-50, bankAccount.getBalance(), 0.001);
    }

    @Test
    @DisplayName("When balance is negative then withdrawal of 100 is allowed")
    public final void whenBalanceIsNegativeThenWithdrawalOf100IsAllowed() {
        bankAccount.setBalance(-50);
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, -50, "");
        bankAccount.makeTransaction(card, transaction);
        Assertions.assertTrue(transaction.isSuccessful());
        Assertions.assertEquals(-185, bankAccount.getBalance(), 0.001);
    }

    @Test
    @DisplayName("When account is inactive then withdrawal is rejected")
    public final void whenAccountIsInactiveThenWithdrawalIsRejected() {
        bankAccount.setActive(false);
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, 500, "");
        bankAccount.makeTransaction(card, transaction);
        Assertions.assertFalse(transaction.isSuccessful());
        Assertions.assertEquals(500, bankAccount.getBalance(), 0.001);
    }
}
