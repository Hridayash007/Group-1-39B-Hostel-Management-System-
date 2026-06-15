package controller;

import dao.UserDao;
import javax.swing.JOptionPane;
import model.UserData;
import view.AddStudentDetails;
import view.IssueComplaints;
import view.LogIn;
import view.MakePayment;
import view.RoomDetailsStudent;
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

        view.setWelcomeUser(user.getUsername());

        // ── Init circular image label (must run after the view is visible) ───
        // We call it immediately; the label is added programmatically on top of
        // the hidden userimage JTextField.
        view.initProfileImageLabel();

        // ── Pre-fill all form fields ─────────────────────────────────────────
        preFillForm();

        // ── Profile image click → open file chooser ──────────────────────────
        view.getProfileImageLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String savedPath = util.ProfileImageHelper.chooseAndSaveImage(user.getId());
                if (savedPath != null) {
                    // Store path in the hidden text field and update preview
                    view.setImagePath(savedPath);
                    view.setProfileImagePreview(savedPath);
                    // Immediately persist to DB so it's not lost if user cancels
                    userDao.updateProfileImage(user.getId(), savedPath);
                    user.setUserImage(savedPath);
                }
            }
        });

        // ── Back to Profile ──────────────────────────────────────────────────
        view.BackToProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });

        // ── Save Changes ─────────────────────────────────────────────────────
        view.SaveChangesListener(e -> saveDetails());

        // ── Cancel ───────────────────────────────────────────────────────────
        view.CancelListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });

        // ── Change Password ──────────────────────────────────────────────────
        view.ChangePasswordListener(e -> {
            close();
            view.ChangePassword changeView = new view.ChangePassword();
            new ChangePasswordController(changeView, user).open();
        });

        // ── Navigation ───────────────────────────────────────────────────────
        view.DashboardListener(e -> {
            close();
            new StudentDashboardController(new StudentDashboard(), user).open();
        });
        view.MyProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });
        view.ProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });
        view.MyComplaintsListener(e -> {
            close();
            new IssueComplaintsController(new IssueComplaints(), user).open();
        });
        view.NoticeListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
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
        
        // ── Sign Out ─────────────────────────────────────────────────────────
        view.SignOutListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to sign out?", "Sign Out",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                close();
                new LoginController(new LogIn()).open();
            }
        });
    }

    // ── Pre-fill ──────────────────────────────────────────────────────────────
    private void preFillForm() {
        view.getFullNameField().setText(
                (user.getFullName() != null && !user.getFullName().isEmpty())
                        ? user.getFullName() : user.getUsername());
        view.getMailField().setText(user.getEmail());
        view.getMailField().setEditable(false);

        setText(view.getNumberField(),        user.getPhone());
        setText(view.getDOBField(),           user.getDateOfBirth());
        setText(view.getCountryField(),       user.getNationality());
        setText(view.getCourseField(),        user.getProgram());
        setText(view.getContactNameField(),   user.getEcName());
        setText(view.getRelationField(),      user.getEcRelation());
        setText(view.getContactNumberField(), user.getEcNumber());
        setText(view.getAddressField(),       user.getAddress());

        setCombo(view.getYearOfStudyField(), user.getYearOfStudy());
        setCombo(view.getUsernameField(),    user.getSemester());

        // Show existing profile image in the circular label
        if (user.getUserImage() != null && !user.getUserImage().isEmpty()) {
            view.setImagePath(user.getUserImage());
            view.setProfileImagePreview(user.getUserImage());
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    private void saveDetails() {
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

        // Include whatever image path is stored (may have been set by click earlier)
        String imagePath = view.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            user.setUserImage(imagePath);
        }

        if (user.getFullName().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Full name cannot be empty.");
            return;
        }

        boolean saved = userDao.updateStudentDetails(user);
        if (saved) {
            UserData fresh = userDao.getStudentDetails(user.getId());
            if (fresh != null) user = fresh;

            JOptionPane.showMessageDialog(view, "Details saved successfully!",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            close();
            // Navigate to profile — image will now render from the saved path
            new StudentProfileController(new StudentProfile(), user).open();
        } else {
            JOptionPane.showMessageDialog(view,
                    "Failed to save details. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void setText(javax.swing.JTextField f, String v) {
        if (v != null && !v.isEmpty()) f.setText(v);
    }

    @SuppressWarnings("unchecked")
    private void setCombo(javax.swing.JComboBox combo, String value) {
        if (value == null || value.isEmpty()) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).toString().equals(value)) {
                combo.setSelectedIndex(i); return;
            }
        }
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}