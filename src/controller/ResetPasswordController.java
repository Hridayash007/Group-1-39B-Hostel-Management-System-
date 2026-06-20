package controller;

import dao.UserDao;
import model.SessionData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import view.PasswordResetSuccessful;
import view.ResetPassword;
import view.VerifyOtp;

public class ResetPasswordController {

    private final UserDao userDao = new UserDao();
    private final ResetPassword view;

    public ResetPasswordController(ResetPassword view) {
        this.view = view;
        view.ResetPasswordListener(new ResetPasswordListener());
        view.backButtonListener(new BackListener());
    }

    public void open() {
        view.setVisible(true);
    }

    public void close() {
        view.dispose();
    }

    class ResetPasswordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newPassword = view.NewPasswordField().getText().trim();
            String confirmPassword = view.ConfirmNewPasswordField().getText().trim();

            // Validation
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Please fill in both password fields.");
                return;
            }

            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(view, "Password must be at least 6 characters long.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(view,
                        "Passwords do not match. Please try again.",
                        "Mismatch", JOptionPane.ERROR_MESSAGE);
                view.ConfirmNewPasswordField().setText("");
                return;
            }

            String email = SessionData.currentEmail;
            if (email == null || email.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                        "Session expired. Please start the reset process again.",
                        "Session Error", JOptionPane.ERROR_MESSAGE);
                close();
                return;
            }

            boolean updated = userDao.updatePassword(email, newPassword);

            if (updated) {
                // Clear session data
                SessionData.currentEmail = "";
                SessionData.currentOTP = "";

                close();
                PasswordResetSuccessful successView = new PasswordResetSuccessful();
                new PasswordResetSuccessfulController(successView).open();
            } else {
                JOptionPane.showMessageDialog(view,
                        "Failed to update password. Please try again.",
                        "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class BackListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
            // Go back to VerifyOtp screen (regenerate OTP display)
            VerifyOtp verifyView = new VerifyOtp();
            new VerifyOtpController(verifyView).open();
        }
    }
}