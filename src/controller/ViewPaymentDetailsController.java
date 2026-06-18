package controller;

import dao.FeeDao;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.UserData;
import view.IssueComplaints;
import view.LogIn;
import view.MakePayment;
import view.RoomDetailsStudent;
import view.StudentDashboard;
import view.StudentMealRoutine;
import view.StudentProfile;
import view.ViewNotice;
import view.ViewPaymentDetails;

public class ViewPaymentDetailsController {

    private final ViewPaymentDetails view;
    private final UserData user;
    private final FeeDao feeDao = new FeeDao();

    public ViewPaymentDetailsController(ViewPaymentDetails view, UserData user) {
        this.view = view;
        this.user = user;

        view.setWelcomeUser(user.getUsername());
        loadPaymentHistory();

        // ── Navigation ────────────────────────────────────────────────────────
        view.DashboardListener(e -> {
            close();
            new StudentDashboardController(new StudentDashboard(), user).open();
        });
        view.MyComplaintsListener(e -> {
            close();
            new IssueComplaintsController(new IssueComplaints(), user).open();
        });
       
        view.NoticeListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        view.RoomDetailsListener(e -> {
            close();
            new RoomDetailsStudentController(new RoomDetailsStudent(), user).open();
        });
        view.MakePaymentListener(e -> {
            close();
            new MakePaymentController(new MakePayment(), user).open();
        });
        view.PaymentHistoryListener(e -> loadPaymentHistory()); // refresh in place
        view.MyProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });
        view.ProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });
        
         //meal routine
        view.MealRoutineListener(e -> {
            close();
            new StudentMealRoutineController(new StudentMealRoutine(),user).open();
        });
        
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

    private void loadPaymentHistory() {
        List<Object[]> history = feeDao.getPaidFees(user.getId());
        String[] cols = {"Payment ID", "Date", "Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : history) model.addRow(row);
        view.getPaymentHistoryTable().setModel(model);
        view.getPaymentHistoryTable().setRowHeight(30);
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}