package controller;

import model.EmailService;
import model.SessionData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import view.ForgetPassword;
import view.ResetPassword;
import view.VerifyOtp;

public class VerifyOtpController {

    private final VerifyOtp view;

    public VerifyOtpController(VerifyOtp view) {
        this.view = view;
        // Show the email address on the label
        view.setEmailLabel(SessionData.currentEmail);

        view.VerifyListener(new VerifyListener());
        view.backListener(new BackListener());
        view.ResendListener(new ResendListener());
    }

    public void open() {
        view.setVisible(true);
    }

    public void close() {
        view.dispose();
    }

    class VerifyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Collect OTP from all 6 fields
            String enteredOtp = view.Code1Field().getText().trim()
                    + view.Code2Field().getText().trim()
                    + view.Code3Field().getText().trim()
                    + view.Code4Field().getText().trim()
                    + view.Code5Field().getText().trim()
                    + view.Code6Field().getText().trim();

            if (enteredOtp.length() < 6) {
                JOptionPane.showMessageDialog(view, "Please enter the complete 6-digit OTP.");
                return;
            }

            if (SessionData.currentOTP == null || SessionData.currentOTP.isEmpty()) {
                JOptionPane.showMessageDialog(view, "OTP has expired. Please request a new one.");
                return;
            }

            if (enteredOtp.equals(SessionData.currentOTP)) {
                // OTP matched — clear it so it can't be reused
                SessionData.currentOTP = "";
                JOptionPane.showMessageDialog(view,
                        "OTP verified successfully!",
                        "Verified", JOptionPane.INFORMATION_MESSAGE);
                close();
                // Open ResetPassword screen
                ResetPassword resetView = new ResetPassword();
                new ResetPasswordController(resetView).open();
            } else {
                JOptionPane.showMessageDialog(view,
                        "Incorrect OTP. Please try again.",
                        "Invalid OTP", JOptionPane.ERROR_MESSAGE);
                // Clear the fields for re-entry
                view.Code1Field().setText("");
                view.Code2Field().setText("");
                view.Code3Field().setText("");
                view.Code4Field().setText("");
                view.Code5Field().setText("");
                view.Code6Field().setText("");
                view.Code1Field().requestFocus();
            }
        }
    }

    class BackListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
            ForgetPassword forgetView = new ForgetPassword();
            new ForgetPasswordController(forgetView).open();
        }
    }

    class ResendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = SessionData.currentEmail;

            if (email == null || email.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Session expired. Please start over.");
                close();
                ForgetPassword forgetView = new ForgetPassword();
                new ForgetPasswordController(forgetView).open();
                return;
            }

            String newOtp = EmailService.generateOTP();
            SessionData.currentOTP = newOtp;

            boolean sent = EmailService.sendOTPEmail(email, newOtp);

            if (sent) {
                // Clear OTP fields
                view.Code1Field().setText("");
                view.Code2Field().setText("");
                view.Code3Field().setText("");
                view.Code4Field().setText("");
                view.Code5Field().setText("");
                view.Code6Field().setText("");
                view.Code1Field().requestFocus();

                JOptionPane.showMessageDialog(view,
                        "A new OTP has been sent to: " + email,
                        "OTP Resent", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view,
                        "Failed to resend OTP. Please try again.",
                        "Email Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
