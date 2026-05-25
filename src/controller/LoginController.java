package controller;

import dao.UserDao;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import model.UserData;
import view.LogIn;
import view.ForgetPassword;

public class LoginController {

    private final UserDao userDao = new UserDao();
    private final LogIn userView;

    public LoginController(LogIn userView) {
        this.userView = userView;
        userView.LoginListener(new LoginListener());
        userView.SignupListener(new SignupListener());
        userView.ForgetPasswordListener(new ForgetPasswordListener());
        
    }

    public void open() {
        this.userView.setVisible(true);
    }

    public void close() {
        this.userView.dispose();
    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String email = userView.getEmailField().getText().trim();
 
           
                String password = new String(userView.getPasswordField().getPassword()).trim();

               if (email.isEmpty() || password.isEmpty() || password.equals("********") || password.equals("8888888888")) {
                    JOptionPane.showMessageDialog(userView, "Please enter your email and password.");
                    return;
                }

                UserData loggedInUser = userDao.loginUser(email, password);

                if (loggedInUser != null) {
                    JOptionPane.showMessageDialog(userView, "Welcome, " + loggedInUser.getUsername() + "!");
                    close();
                    // TODO: Open dashboard here
                    // new DashboardController(new Dashboard(loggedInUser)).open();
                } else {
                    JOptionPane.showMessageDialog(userView, "Invalid email or password. Please try again.");
                }

            } catch (Exception ex) {
                System.out.println("Login error: " + ex.getMessage());
                JOptionPane.showMessageDialog(userView, "Error: " + ex.getMessage());
            }
        }
    }

    class SignupListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
            view.UserRegistration signupView = new view.UserRegistration();
            SignupController signupController = new SignupController(signupView);
            signupController.open();
        }
    }
    
    class ForgetPasswordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
            ForgetPassword forgetView = new ForgetPassword();
            new ForgetPasswordController(forgetView).open();
        }
    }
}

