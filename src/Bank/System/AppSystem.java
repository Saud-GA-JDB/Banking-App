package Bank.System;
import Bank.Users.User;

public class AppSystem {
    private boolean isLoggedIn;
//    private Role role;
    private User.Role role;
    private String cpr;

    public AppSystem (String cpr, User.Role role) {
        this.cpr = cpr;
        this.role = role;
        isLoggedIn = false;
    }

    /*
    =============================================================================
    Setters and getters
    =============================================================================
     */

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    public String getCpr() {
        return cpr;
    }

    public void setCpr(String cpr) {
        this.cpr = cpr;
    }
}
