package Bank.System;

import Bank.Bank;
import Bank.Banking.BankAccount;
import Bank.Banking.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class ScreenTest {
    Screen screen;
    Bank bank;
    BankAccount bankAccount;
    ByteArrayOutputStream output;
    InputStream originalInput;
    PrintStream originalOutput;

    @BeforeEach
    public void setUp() {
        originalInput = System.in;
        originalOutput = System.out;
        output = new ByteArrayOutputStream();
        System.setIn(new ByteArrayInputStream("2\n".getBytes()));
        System.setOut(new PrintStream(output));
        screen = new Screen();
        bank = new Bank("Test Bank");
        bankAccount = new BankAccount("Savings", BankAccount.Type.SAVINGS);
    }

    @AfterEach
    public void tearDown() {
        System.setIn(originalInput);
        System.setOut(originalOutput);
    }

    @Test
    @DisplayName("When a menu option is entered then the selected option is returned")
    public final void whenMenuOptionIsEnteredThenSelectedOptionIsReturned() {
        Assertions.assertEquals(2, screen.customerDashBoardPage(bank));
    }

    @Test
    @DisplayName("When customer menu is displayed then banking services are shown")
    public final void whenCustomerMenuIsDisplayedThenBankingServicesAreShown() {
        screen.customerDashBoardPage(bank);
        Assertions.assertTrue(output.toString().contains("Deposit"));
        Assertions.assertTrue(output.toString().contains("Withdraw"));
        Assertions.assertTrue(output.toString().contains("Transfer"));
        Assertions.assertFalse(output.toString().contains("Customer Services"));
    }

    @Test
    @DisplayName("When banker menu is displayed then customer services are shown")
    public final void whenBankerMenuIsDisplayedThenCustomerServicesAreShown() {
        screen.bankerDashBoardPage(bank);
        Assertions.assertTrue(output.toString().contains("Customer Services"));
        Assertions.assertTrue(output.toString().contains("My Banking"));
    }

    @Test
    @DisplayName("When a statement is displayed then account and transaction details are shown")
    public final void whenStatementIsDisplayedThenAccountAndTransactionDetailsAreShown() {
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.DEPOSITOWN, null, bankAccount.getAccountId(), 100, "Deposit");
        transaction.setSuccessful(true);
        bankAccount.setBalance(100);
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        screen.statementPage(bank, bankAccount, transactions);
        Assertions.assertTrue(output.toString().contains("Account: Savings"));
        Assertions.assertTrue(output.toString().contains("Current Balance: $100.0"));
        Assertions.assertTrue(output.toString().contains("Transactions: 1"));
        Assertions.assertTrue(output.toString().contains(transaction.getTransactionId().toString()));
        Assertions.assertTrue(output.toString().contains("Amount: $100.0"));
    }

    @Test
    @DisplayName("When a transaction finishes then its result is displayed")
    public final void whenTransactionFinishesThenItsResultIsDisplayed() {
        Transaction transaction = new Transaction(100, Transaction.TransactionTypes.WITHDRAW, bankAccount.getAccountId(), null, 400, "Withdrawal");
        transaction.setSuccessful(true);
        screen.transactionResultPage(bank, transaction);
        Assertions.assertTrue(output.toString().contains(transaction.getTransactionId().toString()));
        Assertions.assertTrue(output.toString().contains("Type: WITHDRAW"));
        Assertions.assertTrue(output.toString().contains("Amount: $100.0"));
        Assertions.assertTrue(output.toString().contains("Balance: $400.0"));
        Assertions.assertTrue(output.toString().contains("Successful: true"));
    }
}
