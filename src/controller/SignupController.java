package controller;

import dao.UserDao;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import model.UserData;
import view.LogIn;
import view.UserRegistration;

public class SignupController {

    private final UserDao userDao = new UserDao();
    private final UserRegistration userView;

    public SignupController(UserRegistration userView) {
        this.userView = userView;
        userView.AddUserListener(new AddUserListener());
        userView.LoginListener(new LoginListener());
    }

    public void open() {
        this.userView.setVisible(true);
    }

    public void close() {
        this.userView.dispose();
    }

    class AddUserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String username        = userView.getUsernameField().getText().trim();
                String email           = userView.getEmailField().getText().trim();
                String password        = userView.getPasswordText();        
                String confirm_password = userView.getConfirmPasswordText(); 

                // Validation
                if (username.isEmpty() || email.isEmpty() || 
                    password.isEmpty() || confirm_password.isEmpty()) {
                    JOptionPane.showMessageDialog(userView, "All fields are required.");
                    return;
                }
                
                if(!email.endsWith("@gmail.com")){
                   JOptionPane.showMessageDialog(userView, "Invalid Email.");
                    return; 
                }
                
                if(password.equals("********") ){
                   JOptionPane.showMessageDialog(userView, "Enter Password.");
                    return; 
                }
                
                if(password.length()<6 ){
                    JOptionPane.showMessageDialog(userView, "Password must be atleast 6 characters");
                    return; 
                }
                    
                if (!password.equals(confirm_password)) {
                    JOptionPane.showMessageDialog(userView, "Passwords do not match.");
                    return;
                }

                UserData user = new UserData(username, email, password, confirm_password);

                boolean exists = userDao.checkUser(user);
                if (exists) {
                    JOptionPane.showMessageDialog(userView, "Email or username already registered.");
                } else {
                    userDao.createUser(user);
                    JOptionPane.showMessageDialog(userView, "Registration Successful! Please log in.");
                    close();
                    LogIn loginView = new LogIn();
                    new LoginController(loginView).open();
                }

            } catch (Exception ex) {
                System.out.println("Signup error: " + ex.getMessage());
                JOptionPane.showMessageDialog(userView, "Error: " + ex.getMessage());
            }
        }
    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
            LogIn loginView = new LogIn();
            new LoginController(loginView).open();
        }
    }
}
