package controller;

import dao.FeeDao;
import dao.PaymentDao;
import model.PaymentData;
import model.UserData;
import view.*;

import javax.swing.JOptionPane;
import java.util.List;

public class MakePaymentController {
    private final MakePayment view;
    private final FeeDao dao = new FeeDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final UserData user;
    private final int userId;

    public MakePaymentController(MakePayment view, UserData user) {
        this.view = view;
        this.user = user;
        this.userId = user.getId();

        view.setWelcomeUser(user.getUsername());
        loadFees();
        loadPaymentHistory();

        view.PayNowListener(e -> pay());

        // ==========================
        // Navigation
        // ==========================


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

    // ==========================
    // Load Pending Fees
    // ==========================


    private void loadFees() {
        view.clearPendingPanel();

        List<Object[]> fees = dao.getPendingFees(userId);
        if (fees.isEmpty()) return;

        for (Object[] f : fees) {
            int id = Integer.parseInt(f[0].toString());
            String studentName = String.valueOf(f[1]);
            String room = String.valueOf(f[2]);
            double amount = f[3] != null ? Double.parseDouble(f[3].toString()) : 0;
            view.addPendingFee(studentName, room, amount, id);
        }
    }

    // Payment

    private void pay() {
        // Read the current fee from the view — never from a stale instance field.
        int feeId = view.getCurrentFeeId();
        double amount = view.getCurrentAmount();

        if (feeId == 0) {
            JOptionPane.showMessageDialog(view, "No pending fee to pay.");
            return;
        }

        // Disable the button immediately to prevent double-clicks firing twice.
        view.setPayNowEnabled(false);

        boolean marked = dao.markPaid(feeId);
        if (marked) {
            PaymentData p = new PaymentData();
            p.setUserId(userId);
            p.setFeeId(feeId);
            p.setAmount(amount);
            p.setStripeSessionId("");
            p.setStatus("Paid");
            paymentDao.savePayment(p);

            JOptionPane.showMessageDialog(view, "Payment successful!");
            view.clearPendingPanel();   // also resets currentFeeId → 0
            loadFees();
            loadPaymentHistory();
        } else {
            JOptionPane.showMessageDialog(view, "Payment failed. Please try again.");
            view.setPayNowEnabled(true);  // re-enable only on failure
        }
    }

    private void loadPaymentHistory() {
        List<Object[]> history = dao.getPaidFees(userId);
        String[] cols = {"Payment ID", "Payment Date", "Amount", "Status"};
        javax.swing.table.DefaultTableModel model =
                new javax.swing.table.DefaultTableModel(cols, 0) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                };
        for (Object[] row : history) model.addRow(row);
        view.getPaymentHistoryTable().setModel(model);
        view.getPaymentHistoryTable().setRowHeight(28);
    }

    public void open(){

        view.setVisible(true);

    }

    private void close(){

        view.dispose();

    }


}