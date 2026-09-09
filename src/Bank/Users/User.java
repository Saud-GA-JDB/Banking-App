package Bank.Users;

import java.util.Date;

public abstract class User {
    private String fName;
    private String lName;
    private Date dateOfBirth;
    private long cpr;
    private String hashedPassword;
    private String securityQuestion;
    private String hashedSecurityQuestionAnswer;
    private int failedLoginAttempts;
    private int lockoutTimeInMin;
    private boolean isLockedOut;

    public User(String fName, String lName, Date dateOfBirth, long cpr, String hashedPassword, String securityQuestion, String hashedSecurityQuestionAnswer) {
        this.fName = fName;
        this.lName = lName;
        this.dateOfBirth = dateOfBirth;
        this.cpr = cpr;
        this.hashedPassword = hashedPassword;
        this.securityQuestion = securityQuestion;
        this.hashedSecurityQuestionAnswer = hashedSecurityQuestionAnswer;
        failedLoginAttempts = 0;
        lockoutTimeInMin = 1;
        isLockedOut = false;
    }

    /*
    =============================================================================
    Setters and getters
    =============================================================================
     */

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public long getCpr() {
        return cpr;
    }

    public void setCpr(long cpr) {
        this.cpr = cpr;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getHashedSecurityQuestionAnswer() {
        return hashedSecurityQuestionAnswer;
    }

    public void setHashedSecurityQuestionAnswer(String hashedSecurityQuestionAnswer) {
        this.hashedSecurityQuestionAnswer = hashedSecurityQuestionAnswer;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public int getLockoutTimeInMin() {
        return lockoutTimeInMin;
    }

    public void setLockoutTimeInMin(int lockoutTimeInMin) {
        this.lockoutTimeInMin = lockoutTimeInMin;
    }

    public boolean isLockedOut() {
        return isLockedOut;
    }

    public void setLockedOut(boolean lockedOut) {
        isLockedOut = lockedOut;
    }

    /*
    =============================================================================
    Methods
    =============================================================================
     */
}
