package Bank.Users;

import java.util.Date;

public class Customer extends User {

    public Customer(String fName, String lName, Date dateOfBirth, long cpr, String hashedPassword, String securityQuestion, String hashedSecurityQuestionAnswer) {
        super(fName, lName, dateOfBirth, cpr, Role.CUSTOMER, hashedPassword, securityQuestion, hashedSecurityQuestionAnswer);
    }
}
