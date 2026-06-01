package controller;

import dao.UserDao;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import model.AdminCredentials;
import model.UserData;
import view.ForgetPassword;
import view.LogIn;
import view.NoticeAdmin;
import view.StudentDashboard;

public class LoginController {

    private final UserDao userDao = new UserDao();
    private final LogIn userView;

    public LoginController(LogIn userView) {
        this.userView = userView;
        userView.LoginListener(new LoginListener());
        userView.SignupListener(new SignupListener());
        userView.ForgetPasswordListener(new ForgetPasswordListener());
    }

    public void open()  { userView.setVisible(true); }
    public void close() { userView.dispose(); }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String email    = userView.getEmailField().getText().trim();
                String password = new String(userView.getPasswordField().getPassword()).trim();

                if (email.isEmpty() || password.isEmpty()
                        || password.equals("**********") || password.equals("8888888888")) {
                    JOptionPane.showMessageDialog(userView, "Please enter your email and password.");
                    return;
                }

                // ── Check admin first ────────────────────────────────────────
                if (AdminCredentials.isAdmin(email, password)) {
                    close();
                    NoticeAdmin adminView = new NoticeAdmin();
                    new NoticeAdminController(adminView).open();
                    return;
                }

                // ── Regular student login ────────────────────────────────────
                UserData loggedInUser = userDao.loginUser(email, password);

                if (loggedInUser != null) {
                    close();
                    StudentDashboard dashboardView = new StudentDashboard();
                    new StudentDashboardController(dashboardView, loggedInUser).open();
                } else {
                    JOptionPane.showMessageDialog(userView,
                            "Invalid email or password. Please try again.",
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
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
            new SignupController(signupView).open();
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