package controller;

import dao.UserDao;
import javax.swing.JOptionPane;
import model.UserData;
import view.AddStudentDetails;
import view.IssueComplaints;
import view.LogIn;
import view.StudentDashboard;
import view.StudentProfile;
import view.ViewNotice;

public class AddStudentDetailsController {

    private final UserDao userDao = new UserDao();
    private final AddStudentDetails view;
    private UserData user;

    public AddStudentDetailsController(AddStudentDetails view, UserData user) {
        this.view = view;
        this.user = user;
        
        // Display the logged-in user's name on the dashboard
        view.setWelcomeUser(user.getUsername());
        preFillForm();

        // ── "Back to Profile" button ─────────────────────────────────────────
        view.BackToProfileListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });

        // ── Save Changes ─────────────────────────────────────────────────────
        view.SaveChangesListener(e -> saveDetails());

        // ── Cancel → back to profile ─────────────────────────────────────────
        view.CancelListener(e -> {
            close();
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        });
        //-Change password
        view.ChangePasswordListener(e -> {
            close();
            view.ChangePassword changeView = new view.ChangePassword();
            new ChangePasswordController(changeView, user).open();
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
                LogIn loginView = new LogIn();
                new LoginController(loginView).open();
            }
        });
    }

    /**
     * Pre-fills form fields with existing user data.
     * Email is auto-filled from registration and made read-only.
     */
    private void preFillForm() {
        // Always available from registration
        view.getFullNameField().setText(
                (user.getFullName() != null && !user.getFullName().isEmpty())
                        ? user.getFullName() : user.getUsername());
        view.getMailField().setText(user.getEmail());
        view.getMailField().setEditable(false); // email comes from registration

        // Extended fields — pre-fill if already set
        setText(view.getNumberField(),        user.getPhone());
        setText(view.getDOBField(),           user.getDateOfBirth());
        setText(view.getCountryField(),       user.getNationality());
        setText(view.getCourseField(),        user.getProgram());
        setText(view.getContactNameField(),   user.getEcName());
        setText(view.getRelationField(),      user.getEcRelation());
        setText(view.getContactNumberField(), user.getEcNumber());
        setText(view.getAddressField(),       user.getAddress());

        // ComboBoxes
        setCombo(view.getYearOfStudyField(), user.getYearOfStudy());
        setCombo(view.getUsernameField(),    user.getSemester());
    }

    private void saveDetails() {
        // Collect all values from form into the user object
        user.setFullName(   view.getFullNameField().getText().trim());
        user.setEmail(      view.getMailField().getText().trim());
        user.setPhone(      view.getNumberField().getText().trim());
        user.setDateOfBirth(view.getDOBField().getText().trim());
        user.setNationality(view.getCountryField().getText().trim());
        user.setProgram(    view.getCourseField().getText().trim());
        user.setYearOfStudy(view.getYearOfStudyField().getSelectedItem().toString());
        user.setSemester(   view.getUsernameField().getSelectedItem().toString());
        user.setEcName(     view.getContactNameField().getText().trim());
        user.setEcRelation( view.getRelationField().getText().trim());
        user.setEcNumber(   view.getContactNumberField().getText().trim());
        user.setAddress(    view.getAddressField().getText().trim());

        if (user.getFullName().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Full name cannot be empty.");
            return;
        }

        boolean saved = userDao.updateStudentDetails(user);

        if (saved) {
            // Re-fetch fresh data from DB so profile shows latest values
            UserData fresh = userDao.getStudentDetails(user.getId());
            if (fresh != null) user = fresh;

            JOptionPane.showMessageDialog(view,
                    "Details saved successfully!",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            close();
            // Navigate back to profile — it will show the updated values
            StudentProfile profileView = new StudentProfile();
            new StudentProfileController(profileView, user).open();
        } else {
            JOptionPane.showMessageDialog(view,
                    "Failed to save details. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void setText(javax.swing.JTextField field, String value) {
        if (value != null && !value.isEmpty()) field.setText(value);
    }

    private void setCombo(javax.swing.JComboBox combo, String value) {
        if (value == null || value.isEmpty()) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).toString().equals(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}