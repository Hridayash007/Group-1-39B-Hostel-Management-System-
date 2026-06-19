package controller;

import dao.RoomDao;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
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
import model.RoomData;
import view.AdminDasboard;
import view.AdminMenuAdjustment;
import view.LogIn;
import view.NoticeAdmin;
import view.RoomAllocation1;
import view.RoomDetails;
import view.RoomDetailsExpand;

public class RoomDetailsController {

    private final RoomDao roomDao = new RoomDao();
    private final RoomDetails view;
    private String currentFilter = "All";

    public RoomDetailsController(RoomDetails view) {
        this.view = view;
        refresh();

        // ── Add Room button ──────────────────────────────────────────────────
        view.AddRoomListener(e -> {
            RoomDetailsExpand form = new RoomDetailsExpand();
            new RoomDetailsExpandController(form, this).open(view);
        });

        // ── Filter buttons ───────────────────────────────────────────────────
        view.AllListener(e     -> { currentFilter = "All";     refresh(); });
        view.BlockAListener(e  -> { currentFilter = "Block A"; refresh(); });
        view.BlockBListener(e  -> { currentFilter = "Block B"; refresh(); });
        view.BlockCListener(e  -> { currentFilter = "Block C"; refresh(); });
        view.BlockDListener(e  -> { currentFilter = "Block D"; refresh(); });
        view.VacantListener(e  -> { currentFilter = "Vacant";  refresh(); });
        view.PartialListener(e -> { currentFilter = "Partial"; refresh(); });

        // ── Navigation ───────────────────────────────────────────────────────
        view.RoomAllocationListener(e -> {
            close();
            new RoomAllocationController(new RoomAllocation1()).open();
        });
        view.DashboardListener(e -> {
            close();
            new AdminDashboardController(new AdminDasboard()).open();
        });
        view.NoticeListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });
        view.StudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new view.ViewStudentDetails()).open();
        });
        view.ComplaintsListener(e -> {
            close();
            new ViewComplaintController(new view.ViewComplaint()).open();
        });
        
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
        
        //Payment Details
        view.PaymentDetailsListener(e -> {
            close();
            new ViewPaymentDetailsAdminController(new view.ViewPaymentDetailsAdmin()).open();
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
        // Stats
        int totalRooms = roomDao.countTotalRooms();
        int totalCapacity = roomDao.countTotalCapacity();
        int occupiedBeds = roomDao.countOccupiedBeds();
        int vacantBeds = totalCapacity - occupiedBeds;

        view.setTotalRooms(totalRooms);
        view.setTotalCapacity(totalCapacity);
        view.setOccupied(occupiedBeds);
        view.setVacantBeds(vacantBeds);

        // Table
        List<RoomData> rooms = switch (currentFilter) {
            case "Block A", "Block B", "Block C", "Block D" -> roomDao.getRoomsByBlock(currentFilter);
            case "Vacant"  -> roomDao.getAllRooms().stream()
                    .filter(r -> r.getStatus().equals("Vacant")).toList();
            case "Partial" -> roomDao.getAllRooms().stream()
                    .filter(r -> r.getStatus().equals("Partial")).toList();
            default -> roomDao.getAllRooms();
        };
        loadTable(rooms);
    }

    private void loadTable(List<RoomData> rooms) {
        String[] cols = {"Room", "Block", "Floor", "Type", "Occupancy", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        for (RoomData r : rooms) {
            model.addRow(new Object[]{
                r.getRoomNumber(), r.getBlock(), r.getFloor(),
                r.getType(), r.getOccupancyString(), r.getStatus(), r
            });
        }

        JTable table = view.getRoomTable();
        table.setModel(model);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionEditor(rooms, table));
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        // Color status column
        table.getColumnModel().getColumn(5).setCellRenderer((t, val, sel, foc, row, col) -> {
            javax.swing.JLabel lbl = new javax.swing.JLabel(val != null ? val.toString() : "");
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            String s = val != null ? val.toString() : "";
            lbl.setBackground(switch (s) {
                case "Full"    -> new Color(254, 226, 226);
                case "Partial" -> new Color(255, 237, 213);
                default        -> new Color(220, 252, 231);
            });
            return lbl;
        });
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
    public RoomDetails getView() { return view; }

    // ── Action column renderer ────────────────────────────────────────────────
    private class ActionRenderer implements TableCellRenderer {
        private final JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        ActionRenderer() { p.add(makeBtn("🗑 Delete", new Color(220, 38, 38))); }
        private JButton makeBtn(String t, Color fg) {
            JButton b = new JButton(t);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            b.setForeground(fg);
            return b;
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean s, boolean f, int r, int c) {
            p.setBackground(s ? t.getSelectionBackground() : t.getBackground());
            return p;
        }
    }

    // ── Action column editor ──────────────────────────────────────────────────
    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        private final JButton deleteBtn = new JButton("🗑 Delete");
        private final List<RoomData> rooms;
        private final JTable table;
        private int row;

        ActionEditor(List<RoomData> rooms, JTable table) {
            this.rooms = rooms; this.table = table;
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            deleteBtn.setForeground(new Color(220, 38, 38));
            p.add(deleteBtn);

            deleteBtn.addActionListener(e -> {
                stopCellEditing();
                RoomData r = rooms.get(row);
                int confirm = JOptionPane.showConfirmDialog(view,
                        "Delete room " + r.getRoomNumber() + "? This will remove all allocations.",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (roomDao.deleteRoom(r.getRoomId())) {
                        JOptionPane.showMessageDialog(view, "Room deleted.");
                        rooms.remove(row);
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                        refresh();
                    }
                }
            });
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean s, int r, int c) { row = r; return p; }
        @Override public Object getCellEditorValue()        { return null; }
        @Override public boolean isCellEditable(EventObject e) { return true; }
    }
}