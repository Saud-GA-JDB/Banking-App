package Bank.Users;

import java.util.Date;

public class Banker extends User implements IEmployee {
    public Banker(String fName, String lName, Date dateOfBirth, long cpr, String hashedPassword, String securityQuestion, String hashedSecurityQuestionAnswer) {
        super(fName, lName, dateOfBirth, cpr, Role.BANKER, hashedPassword, securityQuestion, hashedSecurityQuestionAnswer);
    }
    @Override
    public void addCustomer(Customer customer) {

    }
}
