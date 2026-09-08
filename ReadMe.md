Entities:
---
* Bank
* BankAccount (checking/savings)
* Card (maybe make diff card types each a separate entity and child of this)
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
* BankAccount
    * Password (encrypted)
    * Id (maybe uuid)
    * name
    * balance
    * SecurityQuestion
    * SecurityQuestionAnswer
    * DateCreated
    * failedLoginAttempts
    * lockoutTime
    * isLockedOut
    * isActive
    * overDraftFee
    * overDraftCount
* Transaction
    * id (uuid) 
    * date
    * time
    * type (withdraw, transfer, deposit)
    * toAccountId
    * postTransactionBalance
    * isSuccessful (did it go through or not)
    * Note (if it failed show why, if successful do something else)
* Card
    * cardType
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


--------------------------------------------------------------------------------
