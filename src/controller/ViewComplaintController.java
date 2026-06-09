package controller;

import dao.ComplaintDao;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import model.ComplaintData;
import view.LogIn;
import view.NoticeAdmin;
import view.ViewComplaint;
import view.ViewComplaintExpand;
import javax.swing.ImageIcon;

public class ViewComplaintController {

    private final ComplaintDao dao = new ComplaintDao();
    private final ViewComplaint view;
    private String currentFilter = "All";

    public ViewComplaintController(ViewComplaint view) {
        this.view = view;

        refresh();

        // ── Filter buttons ───────────────────────────────────────────────────
        view.AllListener     (e -> { currentFilter = "All";      refresh(); });
        view.PendingListener (e -> { currentFilter = "Pending";  refresh(); });
        view.ResolvedListener(e -> { currentFilter = "Resolved"; refresh(); });

        // ── Navigation ───────────────────────────────────────────────────────
        view.NoticeListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });
        view.DashboardListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });
        view.StudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new view.ViewStudentDetails()).open();
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
            new RoomAllocationController(new view.RoomAllocation()).open();
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

    public void refresh() {
        view.setTotalComplaints(dao.countAll());
        view.setPendingCount   (dao.countByStatus("Pending"));
        view.setResolvedCount  (dao.countByStatus("Resolved"));

        List<ComplaintData> list = currentFilter.equals("All")
                ? dao.getAllComplaints()
                : dao.getComplaintsByStatus(currentFilter);

        loadTable(list);
    }

    private void loadTable(List<ComplaintData> list) {
        // 7 columns now — added "Student" before Action
        String[] cols = {"Title", "Student", "Category", "Priority", "Date", "Status", "Action"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        for (ComplaintData c : list) {
            model.addRow(new Object[]{
                c.getTitle(),
                c.getUsername() != null ? c.getUsername() : "—",
                c.getCategory(),
                c.getPriority(),
                c.getFormattedDate(),
                c.getStatus(),
                c       // full object passed to Action column editor
            });
        }

        JTable table = view.getComplaintTable();
        table.setModel(model);
        table.setRowHeight(40);

        // Action column (index 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionEditor(list, table));
        table.getColumnModel().getColumn(6).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setMinWidth(180);
        table.getColumnModel().getColumn(6).setMaxWidth(200);
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }

    // ── Action column renderer — View + Resolve + Reject + Delete ────────────
    private class ActionRenderer implements TableCellRenderer {

    private final JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));

    ActionRenderer() {
        JButton viewBtn = new JButton(new ImageIcon(getClass().getResource("/images/viewnotice.png")));
        JButton resolveBtn = new JButton(new ImageIcon(getClass().getResource("/images/resolved.png")));
        JButton rejectBtn = new JButton(new ImageIcon(getClass().getResource("/images/rejected.png")));
        JButton deleteBtn = new JButton(new ImageIcon(getClass().getResource("/images/deletenotice.png")));

        style(viewBtn);
        style(resolveBtn);
        style(rejectBtn);
        style(deleteBtn);

        p.add(viewBtn);
        p.add(resolveBtn);
        p.add(rejectBtn);
        p.add(deleteBtn);
    }

    private void style(JButton b) {
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new java.awt.Dimension(32, 32));
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        p.setBackground(isSelected
                ? table.getSelectionBackground()
                : table.getBackground());

        return p;
    }
}

    // ── Action column editor ──────────────────────────────────────────────────
    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {

    private final JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));

    private final JButton viewBtn = new JButton();
    private final JButton resolveBtn = new JButton();
    private final JButton rejectBtn = new JButton();
    private final JButton deleteBtn = new JButton();

    private final List<ComplaintData> list;
    private final JTable table;
    private int row;

    ActionEditor(List<ComplaintData> list, JTable table) {
        this.list = list;
        this.table = table;

        viewBtn.setIcon(new ImageIcon(getClass().getResource("/images/viewnotice.png")));
        resolveBtn.setIcon(new ImageIcon(getClass().getResource("/images/resolved.png")));
        rejectBtn.setIcon(new ImageIcon(getClass().getResource("/images/rejected.png")));
        deleteBtn.setIcon(new ImageIcon(getClass().getResource("/images/deletenotice.png")));

        styleBtn(viewBtn, new java.awt.Color(99, 102, 241));
        styleBtn(resolveBtn, new java.awt.Color(34, 197, 94));
        styleBtn(rejectBtn, new java.awt.Color(220, 38, 38));
        styleBtn(deleteBtn, new java.awt.Color(107, 114, 128));

        p.add(viewBtn);
        p.add(resolveBtn);
        p.add(rejectBtn);
        p.add(deleteBtn);
    

            // ── View ──────────────────────────────────────────────────────────
            viewBtn.addActionListener(e -> {
                stopCellEditing();
                ComplaintData c = list.get(row);
                ViewComplaintExpand expandView = new ViewComplaintExpand();
                ViewComplaintExpandController ctrl = new ViewComplaintExpandController(expandView);
                ctrl.setComplaint(c);
                ctrl.open(view);
            });

            // ── Resolve ───────────────────────────────────────────────────────
            resolveBtn.addActionListener(e -> {
                stopCellEditing();
                ComplaintData c = list.get(row);
                if (dao.updateStatus(c.getComplaintId(), "Resolved")) {
                    JOptionPane.showMessageDialog(view, "Complaint marked as Resolved.");
                    refresh();
                }
            });

            // ── Reject ────────────────────────────────────────────────────────
            rejectBtn.addActionListener(e -> {
                stopCellEditing();
                ComplaintData c = list.get(row);
                if (dao.updateStatus(c.getComplaintId(), "Rejected")) {
                    JOptionPane.showMessageDialog(view, "Complaint marked as Rejected.");
                    refresh();
                }
            });

            // ── Delete ────────────────────────────────────────────────────────
            deleteBtn.addActionListener(e -> {
                stopCellEditing();
                ComplaintData c = list.get(row);
                int confirm = JOptionPane.showConfirmDialog(view,
                        "Delete complaint: \"" + c.getTitle() + "\"?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dao.deleteComplaint(c.getComplaintId())) {
                        JOptionPane.showMessageDialog(view, "Complaint deleted.");
                        list.remove(row);
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                        refresh();
                    }
                }
            });
        }

       private void styleBtn(JButton b, java.awt.Color fg) {
    b.setForeground(fg);
    b.setBorderPainted(false);
    b.setContentAreaFilled(false);
    b.setFocusPainted(false);
    b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

    b.setPreferredSize(new java.awt.Dimension(32, 32));
}

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int r, int c) { row = r; return p; }

        @Override public Object getCellEditorValue()          { return null; }
        @Override public boolean isCellEditable(EventObject e){ return true; }
    }
}