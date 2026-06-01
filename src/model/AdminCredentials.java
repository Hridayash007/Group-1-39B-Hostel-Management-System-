package model;
 
/**
 * Change USERNAME / PASSWORD / EMAIL here to update admin login.
 */
public class AdminCredentials {
 
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin123";
    public static final String EMAIL    = "admin@cityscape.com";
 
    /** Returns true if the provided credentials match the fixed admin account. */
    public static boolean isAdmin(String emailOrUsername, String password) {
        return (emailOrUsername.equals(EMAIL) || emailOrUsername.equals(USERNAME))
                && password.equals(PASSWORD);
    }
}
