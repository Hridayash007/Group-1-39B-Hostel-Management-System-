package controller;

import model.UserData;
import view.LogIn;
import view.StudentDashboard;
import view.StudentProfile;
import view.ViewNotice;

public class StudentDashboardController {

    private final StudentDashboard view;
    private final UserData user;

    public StudentDashboardController(StudentDashboard view, UserData user) {
        this.view = view;
        this.user = user;

        view.setWelcomeUser(user.getUsername());

        // ── Notice button ────────────────────────────────────────────────────
        view.NoticeListener(e -> {
            close();
            ViewNotice noticeView = new ViewNotice();
            new ViewNoticeController(noticeView, user).open();
        });

        // ── My Profile ───────────────────────────────────────────────────────
        view.MyProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });

        // ── Profile icon (top-right) ─────────────────────────────────────────
        view.ProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });

        // ── Sign Out ─────────────────────────────────────────────────────────
        view.SignOutListener(e -> {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to sign out?",
                    "Sign Out",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                close();
                new LoginController(new LogIn()).open();
            }
        });
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}