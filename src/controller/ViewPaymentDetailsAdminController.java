package controller;

import dao.FeeDao;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import view.AdminDasboard;
import view.AdminMenuAdjustment;
import view.LogIn;
import view.NoticeAdmin;
import view.RoomAllocation1;
import view.RoomDetails;
import view.ViewComplaint;
import view.ViewPaymentDetailsAdmin;
import view.ViewStudentDetails;

public class ViewPaymentDetailsAdminController {

    private final ViewPaymentDetailsAdmin view;
    private final FeeDao feeDao = new FeeDao();

    public ViewPaymentDetailsAdminController(ViewPaymentDetailsAdmin view) {
        this.view = view;

        // Load all payments on open
        loadPayments(null);

        // ── Filter buttons ────────────────────────────────────────────────────
        view.AllBtnListener(e     -> loadPayments(null));
        view.PendingBtnListener(e -> loadPayments("Pending"));
        view.PaidBtnListener(e    -> loadPayments("Paid"));

        // ── Navigation ────────────────────────────────────────────────────────
        view.DashboardListener(e -> {
            close();
            new AdminDashboardController(new AdminDasboard()).open();
        });
        view.StudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new ViewStudentDetails()).open();
        });
        view.ComplaintsListener(e -> {
            close();
            new ViewComplaintController(new ViewComplaint()).open();
        });
        view.NoticeListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });
        view.RoomDetailsListener(e -> {
            close();
            new RoomDetailsController(new RoomDetails()).open();
        });
        view.RoomAllocationListener(e -> {
            close();
            new RoomAllocationController(new RoomAllocation1()).open();
        });
        view.PaymentDetailsListener(e -> loadPayments(null)); // refresh in place
        
        // top right Notice
        view.NotificationListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });
        
        //meal routine
        view.MealRoutineListener(e -> {
            close();
            new AdminMenuAdjustmentController(new AdminMenuAdjustment()).open();
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

    
    private void loadPayments(String statusFilter) {
        List<Object[]> rows = feeDao.getAllPayments(statusFilter);
        String[] cols = {"Payment ID", "Student", "Room", "Date", "Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : rows) model.addRow(row);
        view.getPaymentHistoryTable().setModel(model);
        view.getPaymentHistoryTable().setRowHeight(30);

        // Colour-code the Status column (index 5)
        view.getPaymentHistoryTable().getColumnModel().getColumn(5).setCellRenderer(
            (table, val, sel, foc, row, col) -> {
                javax.swing.JLabel lbl = new javax.swing.JLabel(
                        val != null ? val.toString() : "");
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                boolean paid = "Paid".equalsIgnoreCase(String.valueOf(val));
                lbl.setBackground(paid
                        ? new java.awt.Color(220, 252, 231)
                        : new java.awt.Color(254, 226, 226));
                lbl.setForeground(paid
                        ? new java.awt.Color(22, 163, 74)
                        : new java.awt.Color(220, 38, 38));
                return lbl;
            });
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}