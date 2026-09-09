Entities:
---
* Bank
* BankAccount (checking/savings)
* Card (maybe make diff card types each a separate entity and child of this)
* PlatinumCard (child of Card)
* MastercardCard (child of Card)
* TitaniumCard (child of Card)
* Person (abstract)
* Banker (child of banker)
* Customer (child of banker)
* Transaction

Relationship:
---
* Bank 1 (customerOf) * Customer
* Bank 1 (accountOf) * BankAccounts
* Bank 1 (has) * Card
* BankAccount * (belongsTo) 1 Person
* Banker [isA] Person
* Customer [isA] Person
* PlatinumCard [isA] Card
* TitaniumCard [isA] Card
* MastercardCard [isA] Card

--------------------------------------------------------------------------------

Attributes:
--
* Bank:
    * name
    * cardTypes (Platinum, Titanium, MasterCard) (thinking about removing them)
* Person
    * fName
    * lName
    * age
    * cpr
    * isLoggedIn
    * Password (encrypted)
    * SecurityQuestion
    * SecurityQuestionAnswer
    * failedLoginAttempts
    * lockoutTime
    * isLockedOut
* BankAccount
    * Id (maybe uuid)
    * name
    * balance
    * DateCreated
    * isActive
    * overDraftFee
    * overDraftCount
* Transaction
    * transactionId (uuid) 
    * transferId (common for both sender and receiver)
    * amount
    * date
    * time
    * type (withdraw, transfer, deposit)
    * toAccountId
    * postTransactionBalance
    * isSuccessful (did it go through or not)
    * Note (if it failed show why, if successful do something else)
* Card
    * cardType
    * cardNumber
    * passcode (6 digits)
    * withdrawLimit
    * transferLimit
    * depositLimit
    * transferLimitOwnAccount
    * depositLimitOwnAccount
    * amountWithdrawnToday
    * amountTransferredToday
    * amountTransferredToOwnAccountToday
    * amountDepositedToday
    * amountDepositedToOwnAccountToday

--------------------------------------------------------------------------------

ERD Diagram:
---
```mermaid
---
config:
  layout: elk
  theme: neutral
  themeVariables:
    fontSize: "12px"
  flowchart:
    nodeSpacing: 8
    rankSpacing: 25
    padding: 5
    curve: linear
    useMaxWidth: true
---
flowchart LR
    %% Entities
    Bank[Bank]
    Person["Person (abstract)"]
    Banker[Banker]
    Customer[Customer]
    Account[BankAccount]
    Transaction[Transaction]
    Card[Card]
    Platinum[PlatinumCard]
    Titanium[TitaniumCard]
    Mastercard[MastercardCard]

    %% Relationships
    Bank ---|"1"| CustomerOf{customerOf}
    CustomerOf ---|"*"| Customer

    Bank ---|"1"| AccountOf{accountOf}
    AccountOf ---|"*"| Account

    Bank ---|"1"| Issues{issues}
    Issues ---|"*"| Card

    Person ---|"1"| Owns{owns}
    Owns ---|"*"| Account

    Account ---|"1"| HasCard{has}
    HasCard ---|"*"| Card

    Account ---|"1"| Records{records}
    Records ---|"*"| Transaction

    Account ---|"0..1"| Destination{destinationOf}
    Destination ---|"*"| Transaction

    %% Inheritance
    PersonISA["△ ISA"]
    Person --- PersonISA
    PersonISA --- Banker
    PersonISA --- Customer

    CardISA["△ ISA"]
    Card --- CardISA
    CardISA --- Platinum
    CardISA --- Titanium
    CardISA --- Mastercard

    %% Bank attributes
    Bank --- B_ID(["bankId (PK)"])
    Bank --- B_Name(["name"])

    %% Person attributes
    Person --- P_CPR(["cpr (PK)"])
    Person --- P_First(["fName"])
    Person --- P_Last(["lName"])
    Person --- P_Age(["age"])
    Person --- P_Login(["isLoggedIn"])
    Person --- P_Password(["passwordHash"])
    Person --- P_Question(["securityQuestion"])
    Person --- P_Attempts(["failedLoginAttempts"])
    Person --- P_LockTime(["lockoutTime"])
    Person --- P_Locked(["isLockedOut"])

    %% BankAccount attributes
    Account --- A_ID(["accountId (PK)"])
    Account --- A_Name(["name"])
    Account --- A_Type(["accountType<br/>checking / savings"])
    Account --- A_Balance(["balance"])
    Account --- A_Answer(["securityQuestion<br/>AnswerHash"])
    Account --- A_Created(["dateCreated"])
    Account --- A_Active(["isActive"])
    Account --- A_Fee(["overDraftFee"])
    Account --- A_Count(["overDraftCount"])

    %% Transaction attributes
    %% transferId is shared by the two transfer records.
    %% destinationOf represents toAccountId.
    Transaction --- T_ID(["transactionId (PK)"])
    Transaction --- T_Transfer(["transferId"])
    Transaction --- T_Amount(["amount"])
    Transaction --- T_Date(["date"])
    Transaction --- T_Time(["time"])
    Transaction --- T_Type(["type<br/>withdraw / transfer / deposit"])
    Transaction --- T_Balance(["postTransactionBalance"])
    Transaction --- T_Success(["isSuccessful"])
    Transaction --- T_Note(["note"])

    %% Card attributes
    Card --- C_ID(["cardId (PK)"])
    Card --- C_Type(["cardType"])
    Card --- C_Passcode(["passcodeHash"])
    Card --- C_WLimit(["withdrawLimit"])
    Card --- C_TLimit(["transferLimit"])
    Card --- C_DLimit(["depositLimit"])
    Card --- C_OTLimit(["transferLimit<br/>OwnAccount"])
    Card --- C_ODLimit(["depositLimit<br/>OwnAccount"])
    Card --- C_Withdrawn(["amountWithdrawn<br/>Today"])
    Card --- C_Transferred(["amountTransferred<br/>Today"])
    Card --- C_OwnTransferred(["amountTransferred<br/>ToOwnAccountToday"])
    Card --- C_Deposited(["amountDeposited<br/>Today"])
    Card --- C_OwnDeposited(["amountDeposited<br/>ToOwnAccountToday"])

    %% Visual styling
    classDef entity fill:#dbeafe,stroke:#2563eb,stroke-width:2px
    classDef relation fill:#fef3c7,stroke:#d97706
    classDef inheritance fill:#dcfce7,stroke:#16a34a
    class Bank,Person,Banker,Customer,Account,Transaction,Card,Platinum,Titanium,Mastercard entity
    class CustomerOf,AccountOf,Issues,Owns,HasCard,Records,Destination relation
    class PersonISA,CardISA inheritance
```

--------------------------------------------------------------------------------
Dir Structure:
--
* in one dir called peopleAndAccount
* one file for customers cpr and maybe name and associated bankAccounts ids and maybe type
* same things for bankers
* in another dir called peopleTransactions has 2 dir, customersTransactions and bankersTransactions.
* in each one it has the cpr as the dir for each person eg. `Banker-<BankerName>-<BankerCpr>`
* in each dir it has a dir titled the year it was created at, and inside another dir for month.
* inside the month dir is a file containing all the transaction in that year,month for that person.

--------------------------------------------------------------------------------

User Stories
---
* as a user, I should be able to login with my credentials and use the system services.
* as a user, I should be able to navigate through the system menu.
* as a logged-in user, I should be able to transfer money to any account.
* as a logged-in user, I should be able to deposit money to my accounts or other peoples account.
* as a logged-in user, I should be able to withdraw money from my accounts.
* as a logged-in user, I should be able to open new accounts/cards.
* as a logged-in user, I should be able to filter transactions.
* as a logged-in user, I should be able to get a detailed account statement.
* as a customer, I should be able to login to my account using my card and passcode.
* as a banker, I should be able to add new customers.
* as a banker, I should be able to reactivate bank accounts. (bonus)
* as a banker, I should be able to reset overdraft counts. (bonus)
* as a banker, I should be able to wave overdraft fees. (bonus)
* as a system, I should be able to authenticate the user credentials against the database.
* as a system, I should be able to display different menus and services against different user roles.
* as a system, I should be able to charge overdraft fee of $35.
* as a system, I should be able to deactivate accounts after 2 overdrafts.
* as a system, I should be able to reactivate accounts after the customer resolves the negative balance.
* as a system, I should be able to detect fraud detection (3 failed login attempts) and lock the account for a time.
* as a system, I should be able to prevent logged-in users from withdrawing more than $100 if the account balance is negative.
* as a system, I should be able to limit bank accounts to one card per account.
* as a system, I should be able to display transaction data after the user finishes a transaction.

--------------------------------------------------------------------------------


Question: 
---
* for transactions, specifically Transfers, why does the limit depend on the card
when i can transfer money without a card?
you still have a card I guess or to the person you are transferring to. 

* is there a limit to the number of accounts a person can have? 
if yes does the type(checking/savings) matter?
no limit

* is there any difference between savings and checking account?
No diff.

* what is the difference between a Customer and a Banker?
a banker has more option than a customer in addition to what the customer can do.
so a banker can create a bank account for a customer, do transaction, etc...

* should customers with deactivated bank accounts be able to deposit and receive money
to their deactivated bank account?
 Yes, but the money will go directly to resolve the negative balance. They also
 won't be able to transfer or withdraw money till the account is reactivated.
 Other functionalities will remain the same.


--------------------------------------------------------------------------------

Notes:
---
* Security questions are inputted by users with the answers when creating account.
they are used when user forget the password so that he can reset the password.

* users choose option by inputting the corresponding number.

* each bank account has its own transactions table.

* think about different databases, and how their managed in the directory
* one database for customers that contain just basic info and other for bankers
* diff dir for each person and each account has a transactions file

* if user is locked out when he attempts to login in, check if lockout time has passed
if yes, change to not locked and allow him entry, otherwise reject.

* inactive accounts should still accept deposits to settle overdrafts and reactivate
  the account

* maybe use enums for account and transaction types

* bankers alone can add customers but customers can open bank accounts and cards.

* maybe in the future make it so when a banker adds a new customer, make the default password
  his password is his cpr, and the customer then has to reset his password. (bonus)


--------------------------------------------------------------------------------

AI Usage:
---
* Used to write the mermaid code for the ERD diagram, input was the section 
before the ERD diagram.
* used to check weather my system design missed anything in the Technical Requirements.

--------------------------------------------------------------------------------

Tools Used:
---
* ChatGPT
* Draw.io
* Trello: https://trello.com/invite/b/6aa1071730cdd15389147058/ATTI975a2d4eb1aecc2fa34faa77848806daC149C793/banking-app