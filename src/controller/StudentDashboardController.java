package controller;

import model.UserData;
import dao.FeeDao;
import dao.RoomDao;
import dao.ComplaintDao;
import dao.NoticeDao;
import model.RoomData;
import view.IssueComplaints;
import view.LogIn;
import view.StudentDashboard;
import view.StudentProfile;
import view.ViewNotice;
import view.RoomDetailsStudent;
import view.MakePayment;
import view.StudentMealRoutine;

public class StudentDashboardController {

    private final StudentDashboard view;
    private final UserData user;
    private final RoomDao roomDao = new RoomDao();
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final NoticeDao noticeDao = new NoticeDao();

    public StudentDashboardController(StudentDashboard view, UserData user) {
        this.view = view;
        this.user = user;

        view.setWelcomeUser(user.getUsername());

        // Generate this month's fee for the student if not already created.
        // Safe to call every login — duplicate-check is inside the SQL.
        new FeeDao().generateMonthlyFees();
        loadDashboardCards();
        // ── My Complaints ────────────────────────────────────────────────────
        view.MyComplaintsListener(e -> {
            close();
            IssueComplaints complaintsView = new IssueComplaints();
            new IssueComplaintsController(complaintsView, user).open();
        });
        
        //QAcomplaint
        view.FileComplaintListener(e -> {
            close();
            IssueComplaints complaintsView = new IssueComplaints();
            new IssueComplaintsController(complaintsView, user).open();
        });

        // ── Notice ───────────────────────────────────────────────────────────
        view.NoticeListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        
        //meal routine
        view.MealRoutineListener(e -> {
            close();
            new StudentMealRoutineController(new StudentMealRoutine(),user).open();
        });
        
        //QA meal routine
        view.QAMealRoutineListener(e -> {
            close();
            new StudentMealRoutineController(new StudentMealRoutine(),user).open();
        });
        
        // ── My Profile ───────────────────────────────────────────────────────
        view.MyProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
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
        
        //QARoomDetails
       view.ViewRoomDetailsListener(e -> {
            close();
            new RoomDetailsStudentController(new RoomDetailsStudent(), user).open();
        });
       
       //QAnotice
        view.CheckNoticeListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        
        //top right notice
        view.NotificatinListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        
        // ── Profile icon (top-right) ─────────────────────────────────────────
        view.ProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });

        // ── Sign Out ─────────────────────────────────────────────────────────
        view.SignOutListener(e -> {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to sign out?", "Sign Out",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE);
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                close();
                new LoginController(new LogIn()).open();
            }
        });
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
    
    private void loadDashboardCards() {
        // Room Number card
        RoomData room = roomDao.getRoomByUser(user.getId());
        if (room != null) {
            view.setRoomNumber(room.getRoomNumber());
            view.setFloor("Floor " + room.getFloor());
        } else {
            view.setRoomNumber("—");
            view.setFloor("No room assigned");
        }

        // Open Complaints card — count of this student's unresolved complaints
        view.setOpenComplaints(complaintDao.countOpenByUser(user.getId()));

        // Unread Notices card — count of notices this student hasn't seen yet
        view.setUnreadNotices(noticeDao.countUnreadByUser(user.getId()));
    }
}