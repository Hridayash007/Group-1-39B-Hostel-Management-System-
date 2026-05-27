package controller;

import model.UserData;
import view.StudentDashboard;
import view.StudentProfile;

public class StudentDashboardController {

    private final StudentDashboard view;
    private final UserData user;

    public StudentDashboardController(StudentDashboard view, UserData user) {
        this.view = view;
        this.user = user;

        // Display the logged-in user's name on the dashboard
        view.setWelcomeUser(user.getUsername());
        
// My Profile button
        view.MyProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });
 
        // Profile icon (top-right) — same as My Profile
        view.ProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });
        // Wire sign-out button back to Login
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
        view.LogIn loginView = new view.LogIn();
        new LoginController(loginView).open();
    }
});
    }

    public void open() {
        view.setVisible(true);
    }

    public void close() {
        view.dispose();
    }
}