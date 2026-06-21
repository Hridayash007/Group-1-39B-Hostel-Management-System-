package controller;

import dao.UserDao;
import util.PasswordUtil;
import javax.swing.JOptionPane;
import model.UserData;
import view.ChangePassword;
import view.IssueComplaints;
import view.LogIn;
import view.MakePayment;
import view.RoomDetailsStudent;
import view.StudentDashboard;
import view.StudentMealRoutine;
import view.StudentProfile;
import view.ViewNotice;

public class ChangePasswordController {

    private final UserDao userDao = new UserDao();
    private final ChangePassword view;
    private final UserData user;

    public ChangePasswordController(ChangePassword view, UserData user) {
        this.view = view;
        this.user = user;

        // Display the logged-in user's name on the dashboard
        view.setWelcomeUser(user.getUsername());

        // ── Update Password ──────────────────────────────────────────────────
        view.UpdatePasswordListener(e -> updatePassword());

        // ── Back to Profile ──────────────────────────────────────────────────
        view.BackToProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });

        // ── Navigation ───────────────────────────────────────────────────────
        view.DashboardListener(e -> {
            close();
            StudentDashboard dashView = new StudentDashboard();
            new StudentDashboardController(dashView, user).open();
        });

        view.MyProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });

        view.ProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });
        
        // ── My Complaints ────────────────────────────────────────────────────
        view.MyComplaintsListener(e -> {
            close();
            IssueComplaints complaintsView = new IssueComplaints();
            new IssueComplaintsController(complaintsView, user).open();
        });

        // ── Notice ───────────────────────────────────────────────────────────
        view.NoticeListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        
        //top right notice
        view.NotificatinListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        
         //meal routine
        view.MealRoutineListener(e -> {
            close();
            new StudentMealRoutineController(new StudentMealRoutine(),user).open();
        });
        
        //--Room Details
        view.RoomDetailsListener(e -> {
            close();
            new RoomDetailsStudentController(new RoomDetailsStudent(), user).open();
        });
        
        //--Make Payment
        view.MakePaymentListener(e -> {
            close();
            new MakePaymentController(new MakePayment(), user).open();
        });
        
        view.PaymentHistoryListener(e -> {
            close();
            new ViewPaymentDetailsController(new view.ViewPaymentDetails(), user).open();
        });
        
        // ── Sign Out ─────────────────────────────────────────────────────────
        view.SignOutListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to sign out?",
                    "Sign Out",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                close();
                new LoginController(new LogIn()).open();
            }
        });
    }

    private void updatePassword() {
        String currentPw  = view.getCurrentPasswordField().getText().trim();
        String newPw       = view.getNewPasswordField().getText().trim();
        String confirmPw   = view.getConfirmNewPasswordField().getText().trim();

        // ── Validation ───────────────────────────────────────────────────────
        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill in all fields.");
            return;
        }

        // BUG FIX: user.getPassword() now holds a BCrypt hash (e.g. "$2a$10$...")
        // for any account created or reset after hashing was added — a direct
        // .equals() against typed plaintext can never match a hash. Mirror the
        // same isHashed()-then-verify logic used in UserDao.loginUser(), so
        // legacy plaintext accounts (left as-is per your instruction) still work.
        String storedPassword = user.getPassword();
        boolean currentPwCorrect = PasswordUtil.isHashed(storedPassword)
                ? PasswordUtil.verify(currentPw, storedPassword)
                : storedPassword.equals(currentPw);

        if (!currentPwCorrect) {
            JOptionPane.showMessageDialog(view,
                    "Current password is incorrect.",
                    "Wrong Password", JOptionPane.ERROR_MESSAGE);
            view.getCurrentPasswordField().setText("");
            return;
        }

        if (newPw.length() < 6) {
            JOptionPane.showMessageDialog(view,
                    "New password must be at least 6 characters.");
            return;
        }

        if (newPw.equals(currentPw)) {
            JOptionPane.showMessageDialog(view,
                    "New password must be different from current password.");
            return;
        }

        if (!newPw.equals(confirmPw)) {
            JOptionPane.showMessageDialog(view,
                    "New password and confirm password do not match.",
                    "Mismatch", JOptionPane.ERROR_MESSAGE);
            view.getConfirmNewPasswordField().setText("");
            return;
        }

        // ── Save to DB ───────────────────────────────────────────────────────
        boolean updated = userDao.updatePassword(user.getEmail(), newPw);

        if (updated) {
            // Note: this generates a fresh hash with a new random salt — it
            // will not be byte-identical to what userDao.updatePassword()
            // wrote to the DB, but that's fine: PasswordUtil.verify() checks
            // a plaintext attempt against ANY valid hash of that password,
            // not hash-to-hash equality. Functionally correct either way.
            user.setPassword(PasswordUtil.hash(newPw));

            JOptionPane.showMessageDialog(view,
                    "Password updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            close();
            // Go back to profile
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        } else {
            JOptionPane.showMessageDialog(view,
                    "Failed to update password. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}