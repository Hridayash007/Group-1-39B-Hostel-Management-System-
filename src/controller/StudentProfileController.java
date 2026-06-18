package controller;

import model.UserData;
import view.AddStudentDetails;
import view.IssueComplaints;
import view.LogIn;
import view.MakePayment;
import view.RoomDetailsStudent;
import view.StudentDashboard;
import view.StudentMealRoutine;
import view.StudentProfile;
import view.ViewNotice;

public class StudentProfileController {

    private final StudentProfile view;
    private UserData user;

    public StudentProfileController(StudentProfile view, UserData user) {
        this.view = view;
        this.user = user;

        populateProfile();

        // ── "edit Profile" button top-right → open AddStudentDetails ────────
        view.EditProfileListener(e -> {
            close();
            AddStudentDetails addView = new AddStudentDetails();
            new AddStudentDetailsController(addView, user).open();
        });

        // ── "Change" password button (Account Security section) ──────────────
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
        
         //meal routine
        view.MealRoutineListener(e -> {
            close();
            new StudentMealRoutineController(new StudentMealRoutine(),user).open();
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
            int confirm = javax.swing.JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to sign out?",
                    "Sign Out",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                close();
                LogIn loginView = new LogIn();
                new LoginController(loginView).open();
            }
        });
    }

    /**
     * Populates ONLY the black value labels — the grey fixed labels are
     * already set in initComponents() and are never touched here.
     */
    private void populateProfile() {
        // Load profile picture into the label (circular crop)
        util.ProfileImageHelper.applyCircularImage(
                view.getProfilePicLabel(), user.getUserImage(), 120);
        String displayName = notEmpty(user.getFullName()) ? user.getFullName() : user.getUsername();
        String studentId   = "Student id-" + user.getId();
        String yearSem     = orDash(user.getYearOfStudy()) + ", " + orDash(user.getSemester()) + " Semester";

        // Header card
        view.setNameDetails(displayName);
        view.setIdLabel(studentId);
        view.setYearsLabel(yearSem);

        // Personal Information section (black value labels only)
        // Fall back to username if full_name not yet filled in AddStudentDetails
        view.setFullNameLabel(notEmpty(user.getFullName()) ? user.getFullName() : user.getUsername());
        view.setEmailLabel(orDash(user.getEmail()));
        view.setPhoneLabel(orDash(user.getPhone()));
        view.setDobLabel(orDash(user.getDateOfBirth()));
        view.setNationalityLabel(orDash(user.getNationality()));

        // Academic Details section (black value labels only)
        view.setProgramLabel(orDash(user.getProgram()));
        view.setYearOfStudyLabel(orDash(user.getYearOfStudy()));
        view.setSemesterLabel(orDash(user.getSemester()));
        view.setStudentIdLabel(String.valueOf(user.getId()));

        // Emergency Contact section (black value labels only)
        String ecDisplay = notEmpty(user.getEcName())
                ? user.getEcName() + (notEmpty(user.getEcRelation()) ? "(" + user.getEcRelation() + ")" : "")
                : "—";
        view.setEmergencyContactLabel(ecDisplay);
        view.setEmergencyNumberLabel(orDash(user.getEcNumber()));
    }

    private String orDash(String val) {
        return (val != null && !val.isEmpty()) ? val : "—";
    }

    private boolean notEmpty(String val) {
        return val != null && !val.isEmpty();
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}