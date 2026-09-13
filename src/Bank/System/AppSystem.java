package Bank.System;
import Bank.Users.User;
// state manager so like a controller in mvc
public class AppSystem {
    private boolean isLoggedIn;
    private User.Role role;
    private String cpr;
    private Screen.Page currentPage;

    public AppSystem () {
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

    public Screen.Page getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Screen.Page currentPage) {
        this.currentPage = currentPage;
    }
    /*
    =============================================================================
    Methods
    =============================================================================
     */

    public void resetAppState() {
        setLoggedIn(false);
        setCpr(null);
        setRole(null);
        setCurrentPage(Screen.Page.START);
    }




    public static void main(String[] args) {

    }
}
