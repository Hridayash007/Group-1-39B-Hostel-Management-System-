package model;

public class UserData {
    private int user_id;
    private String username;  
    private String email;
    private String password;
    private String confirm_password;

    
    public UserData() {}

    
    
    public UserData(String username, String email, String password, String confirm_password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirm_password = confirm_password;
    }

    public int getId() { return user_id; }
    public void setId(int user_id) { this.user_id = user_id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirm_password; }
    public void setConfirmPassword(String confirm_password) { this.confirm_password = confirm_password; }
}