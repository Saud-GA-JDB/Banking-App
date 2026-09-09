package Bank.Banking;

import Bank.Users.Customer;

interface ITransaction {
    void saveCustomer(Customer customer);
    void findCustomerByCpr(String cpr);
    void saveAccount(String cpr, BankAccount account);
    void saveTransaction(String cpr, String accountId, Transaction transaction);
    void loadTransactions(String cpr, String accountId);
}
