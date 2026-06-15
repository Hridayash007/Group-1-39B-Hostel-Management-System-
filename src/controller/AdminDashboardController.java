package controller;

import dao.ComplaintDao;
import dao.NoticeDao;
import dao.UserDao;
import javax.swing.JOptionPane;
import view.AdminDasboard;
import view.LogIn;
import view.NoticeAdmin;
import view.ViewComplaint;
import view.ViewStudentDetails;

public class AdminDashboardController {

    private final UserDao      userDao      = new UserDao();
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final NoticeDao    noticeDao    = new NoticeDao();
    private final AdminDasboard view;

    public AdminDashboardController(AdminDasboard view) {
        this.view = view;

        loadStats();
        wireNavigation();
    }

    // ── Load live stats into the four stat cards ──────────────────────────────
    private void loadStats() {
        view.setTotalStudents  (userDao.getAllStudents().size());
        view.setTotalComplaints(complaintDao.countAll());
        view.setActiveNotices  (noticeDao.countAll());
        // Room occupied is static for now — wire to room DAO when available
    }

    // ── Wire every sidebar button ─────────────────────────────────────────────
    private void wireNavigation() {

        // Dashboard — already here, just reload stats
        view.DashboardListener(e -> loadStats());

        // Students
        view.StudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new ViewStudentDetails()).open();
        });

        // Complaints
        view.ComplaintsListener(e -> {
            close();
            new ViewComplaintController(new ViewComplaint()).open();
        });

        // Notice
        view.NoticeListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });

        // "View all" urgent complaints → opens ViewComplaint
        view.ViewAllComplaintsListener(e -> {
            close();
            new ViewComplaintController(new ViewComplaint()).open();
        });

        // "View all" recent check-ins → opens ViewStudentDetails
        view.ViewAllStudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new ViewStudentDetails()).open();
        });
        
        
        // ── Room Details button ─────────────────────────────────────────────────
        view.RoomDetailsListener(e -> {
            close();
            new RoomDetailsController(new view.RoomDetails()).open();
        }
        );
          
        // ── Room Allocation button ─────────────────────────────────────────────
        view.RoomAllocationListener(e -> {
            close();
            new RoomAllocationController(new view.RoomAllocation1()).open();
        });
        // Sign Out
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

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}
