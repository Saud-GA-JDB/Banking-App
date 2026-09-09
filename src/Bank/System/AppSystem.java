package Bank.System;

public class AppSystem {
    private boolean isLoggedIn;
    enum Role{CUSTOMER, BANKER}
    private Role role;
    private String cpr;

    public AppSystem (String cpr, Role role) {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getCpr() {
        return cpr;
    }

    public void setCpr(String cpr) {
        this.cpr = cpr;
    }
}
