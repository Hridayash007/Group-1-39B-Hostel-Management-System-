package controller;

import dao.UserDao;
import java.awt.Component;
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
import model.UserData;
import view.LogIn;
import view.NoticeAdmin;
import view.ViewStudentDetails;
import view.ViewStudentExpand;
import javax.swing.ImageIcon;
import view.AdminDasboard;

public class ViewStudentDetailsController {

    private final UserDao userDao = new UserDao();
    private final ViewStudentDetails view;

    public ViewStudentDetailsController(ViewStudentDetails view) {
        this.view = view;

        loadStudents();

        // ── Navigation: Dashboard ────────────────────────────────────────────
         view.DashboardListener(e -> {
            close();
            new AdminDashboardController(new AdminDasboard()).open();
        });
         
        
        // ── Complaints button (sidebar) ───────────────────────────────────────
        view.ComplaintsListener(e -> {
            close();
            new ViewComplaintController(new view.ViewComplaint()).open();
        });
        
        
        // ── Navigation: Notice ───────────────────────────────────────────────
        view.NoticeListener(e -> {
            close();
            NoticeAdmin adminView = new NoticeAdmin();
            new NoticeAdminController(adminView).open();
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

    // ── Load all students into the table ──────────────────────────────────────
    private void loadStudents() {
        List<UserData> students = userDao.getAllStudents();

        // Column names matching the existing table model
        String[] columns = {"Students", "Program", "Semester", "Room Number", "Action"};

        // Non-editable model except the Action column
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // only Action column is "editable" (for button clicks)
            }
        };

        for (UserData u : students) {
            String name = (u.getFullName() != null && !u.getFullName().isEmpty())
                    ? u.getFullName() : u.getUsername();
            model.addRow(new Object[]{
                name,
                u.getProgram()     != null ? u.getProgram()     : "—",
                u.getSemester()    != null ? u.getSemester()    : "—",
                "—",               // room number — from room allocation table later
                u                  // store full UserData object for Action column
            });
        }

        JTable table = view.getStudentTable();
        table.setModel(model);
        table.setRowHeight(36);

        // Set Action column renderer and editor
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionEditor(students, table));
        table.getColumnModel().getColumn(4).setPreferredWidth(160);

        // Update total count label
        view.setTotalStudents(students.size());
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }

    // ── Action column renderer — shows View + Delete buttons ─────────────────
    private class ActionRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel();
        private final JButton viewBtn = new JButton();
        private final JButton deleteBtn = new JButton();

        ActionRenderer() {
            panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 4));
            viewBtn.setIcon(new ImageIcon(getClass().getResource("/images/viewnotice.png")));
            deleteBtn.setIcon(new ImageIcon(getClass().getResource("/images/deletenotice.png")));

            viewBtn.setBorderPainted(false);
            viewBtn.setContentAreaFilled(false);

            deleteBtn.setBorderPainted(false);
            deleteBtn.setContentAreaFilled(false);
            panel.add(viewBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            panel.setBackground(isSelected
                    ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }
    }

    // ── Action column editor — handles actual button clicks ───────────────────
    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel    = new JPanel();
        private final JButton viewBtn = new JButton();
        private final JButton deleteBtn = new JButton();
        private final List<UserData> students;
        private final JTable table;
        private int currentRow;

        ActionEditor(List<UserData> students, JTable table) {
            this.students = students;
            this.table    = table;

            panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 4));
            viewBtn.setIcon(new ImageIcon(getClass().getResource("/images/viewnotice.png")));
        deleteBtn.setIcon(new ImageIcon(getClass().getResource("/images/deletenotice.png")));

        viewBtn.setBorderPainted(false);
        viewBtn.setContentAreaFilled(false);

        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
            panel.add(viewBtn);
            panel.add(deleteBtn);

            // ── View → open ViewStudentExpand as modal dialog ─────────────────
            viewBtn.addActionListener(e -> {
                stopCellEditing();
                UserData u = students.get(currentRow);
                ViewStudentExpand expandView = new ViewStudentExpand();
                ViewStudentExpandController ctrl = new ViewStudentExpandController(expandView);
                ctrl.setStudent(u);
                ctrl.open(view);
            });

            // ── Delete → confirm then remove from DB ──────────────────────────
            deleteBtn.addActionListener(e -> {
                stopCellEditing();
                UserData u = students.get(currentRow);
                String name = (u.getFullName() != null && !u.getFullName().isEmpty())
                        ? u.getFullName() : u.getUsername();
                int confirm = JOptionPane.showConfirmDialog(view,
                        "Delete student: \"" + name + "\"?\nThis cannot be undone.",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = userDao.deleteStudent(u.getId());
                    if (deleted) {
                        JOptionPane.showMessageDialog(view, "Student deleted successfully.");
                        students.remove(currentRow);
                        ((DefaultTableModel) table.getModel()).removeRow(currentRow);
                        view.setTotalStudents(students.size());
                    } else {
                        JOptionPane.showMessageDialog(view, "Failed to delete student.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return null; }

        @Override
        public boolean isCellEditable(EventObject e) { return true; }
    }
}