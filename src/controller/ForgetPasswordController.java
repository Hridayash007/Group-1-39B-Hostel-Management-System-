package controller;

import dao.UserDao;
import model.EmailService;
import model.SessionData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import view.ForgetPassword;
import view.LogIn;
import view.VerifyOtp;

public class ForgetPasswordController {

    private final UserDao userDao = new UserDao();
    private final ForgetPassword view;

    public ForgetPasswordController(ForgetPassword view) {
        this.view = view;
        view.SendotpListener(new SendOtpListener());
        view.BacktoLoginListener(new BackToLoginListener());
    }

    public void open() {
        view.setVisible(true);
    }

    public void close() {
        view.dispose();
    }

    class SendOtpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = view.getEmailField().getText().trim();

            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Please enter your email address.");
                return;
            }

            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(view, "Please enter a valid email address.");
                return;
            }

            if (!userDao.checkEmailExists(email)) {
                JOptionPane.showMessageDialog(view, "No account found with this email address.");
                return;
            }

            // Generate OTP and store in session
            String otp = EmailService.generateOTP();
            SessionData.currentOTP = otp;
            SessionData.currentEmail = email;

            // Send OTP email
            boolean sent = EmailService.sendOTPEmail(email, otp);

            if (sent) {
                JOptionPane.showMessageDialog(view,
                        "OTP sent successfully to: " + email,
                        "OTP Sent", JOptionPane.INFORMATION_MESSAGE);
                close();
                // Open VerifyOtp screen
                VerifyOtp verifyView = new VerifyOtp();
                new VerifyOtpController(verifyView).open();
            } else {
                JOptionPane.showMessageDialog(view,
                        "Failed to send OTP. Please check your internet connection and try again.",
                        "Email Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class BackToLoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
            LogIn loginView = new LogIn();
            new LoginController(loginView).open();
        }
    }
}