package controller;

import dao.RoomDao;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import model.RoomData;
import model.UserData;
import view.AdminDasboard;
import view.LogIn;
import view.NoticeAdmin;
import view.RoomAllocation;
import view.RoomDetails;

public class RoomAllocationController {

    private final RoomDao roomDao = new RoomDao();
    private final RoomAllocation view;

    public RoomAllocationController(RoomAllocation view) {
        this.view = view;
        refresh();

        // ── Navigation ───────────────────────────────────────────────────────
        view.RoomDetailsListener(e -> {
            close();
            new RoomDetailsController(new RoomDetails()).open();
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
        view.setStudentCount(roomDao.countUnallocatedStudents());
        view.setRoomCount(roomDao.countRoomsWithVacantBeds());
        view.setAllocationCount(roomDao.countAllocations());

        // Students pending room (left panel)
        loadUnallocatedStudents();

        // Available rooms (right panel)
        loadAvailableRooms();

        // Current allocations table (bottom)
        loadAllocationTable();
    }

    // ── Students with no room ─────────────────────────────────────────────────
    private void loadUnallocatedStudents() {
        List<UserData> students = roomDao.getUnallocatedStudents();
        List<RoomData> rooms    = roomDao.getVacantRooms();

        JPanel container = view.getStudentListPanel();
        container.removeAll();

        int y = 10;
        if (students.isEmpty()) {
            JLabel lbl = new JLabel("All students have been allocated rooms.");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lbl.setForeground(Color.GRAY);
            lbl.setBounds(10, y, 500, 20);
            container.add(lbl);
            y += 30;
        } else {
            for (UserData u : students) {
                JPanel card = buildStudentCard(u, rooms);
                card.setBounds(5, y, 520, 50);
                container.add(card);
                y += 58;
            }
        }

        container.setPreferredSize(new java.awt.Dimension(530, Math.max(y + 10, 60)));
        container.revalidate();
        container.repaint();
    }

    private JPanel buildStudentCard(UserData u, List<RoomData> rooms) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235)));

        // Name
        String displayName = (u.getFullName() != null && !u.getFullName().isEmpty())
                ? u.getFullName() : u.getUsername();
        JLabel nameLbl = new JLabel(displayName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLbl.setBounds(8, 5, 200, 16);
        card.add(nameLbl);

        JLabel progLbl = new JLabel(u.getProgram() != null ? u.getProgram() : "—");
        progLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        progLbl.setForeground(Color.GRAY);
        progLbl.setBounds(8, 22, 150, 14);
        card.add(progLbl);

        // Room dropdown
        String[] roomOptions = rooms.stream()
                .map(r -> r.getRoomNumber() + " (" + r.getBlock() + ", " + r.getType() + ")")
                .toArray(String[]::new);

        if (roomOptions.length == 0) {
            JLabel noRoom = new JLabel("No vacant rooms");
            noRoom.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            noRoom.setForeground(Color.RED);
            noRoom.setBounds(220, 15, 130, 20);
            card.add(noRoom);
        } else {
            JComboBox<String> roomCombo = new JComboBox<>(new DefaultComboBoxModel<>(roomOptions));
            roomCombo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            roomCombo.setBounds(215, 12, 180, 22);
            card.add(roomCombo);

            JButton allocBtn = new JButton("Allocate");
            allocBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            allocBtn.setBackground(new Color(99, 102, 241));
            allocBtn.setForeground(Color.WHITE);
            allocBtn.setBounds(400, 12, 75, 22);
            card.add(allocBtn);

            allocBtn.addActionListener(e -> {
                int selectedIndex = roomCombo.getSelectedIndex();
                if (selectedIndex < 0) return;
                RoomData selectedRoom = rooms.get(selectedIndex);

                int confirm = JOptionPane.showConfirmDialog(view,
                        "Allocate " + displayName + " to room " + selectedRoom.getRoomNumber() + "?",
                        "Confirm Allocation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean done = roomDao.allocateRoom(selectedRoom.getRoomId(), u.getId());
                    if (done) {
                        JOptionPane.showMessageDialog(view,
                                displayName + " allocated to room " + selectedRoom.getRoomNumber() + "!");
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(view,
                                "Allocation failed. Student may already have a room.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        return card;
    }

    // ── Available rooms (right panel) ─────────────────────────────────────────
    private void loadAvailableRooms() {
        List<RoomData> rooms = roomDao.getVacantRooms();
        JPanel container = view.getRoomsPanel();
        container.removeAll();

        int y = 10;
        if (rooms.isEmpty()) {
            JLabel lbl = new JLabel("No vacant rooms available.");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lbl.setForeground(Color.GRAY);
            lbl.setBounds(10, y, 300, 20);
            container.add(lbl);
            y += 30;
        } else {
            for (RoomData r : rooms) {
                JPanel card = buildRoomCard(r);
                card.setBounds(5, y, 540, 65);
                container.add(card);
                y += 58;
            }
        }

        container.setPreferredSize(new java.awt.Dimension(550, Math.max(y + 10, 60)));
        container.revalidate();
        container.repaint();
    }

    private JPanel buildRoomCard(RoomData r) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(new Color(240, 253, 244)); 
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(187, 247, 208)));

        JLabel roomLbl = new JLabel("Room " + r.getRoomNumber());
roomLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
roomLbl.setBounds(8, 5, 120, 16);
card.add(roomLbl);


JLabel detailLbl = new JLabel(r.getBlock() + " · Floor " + r.getFloor()
        + " · " + r.getType() + " · " + r.getOccupancyString());
detailLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
detailLbl.setForeground(new Color(75, 85, 99));
detailLbl.setBounds(8, 22, 400, 14);
card.add(detailLbl);


JLabel facilLbl = new JLabel(
        r.getFacilities().isEmpty() ? "" : "Facilities: " + r.getFacilities()
);
facilLbl.setFont(new Font("Segoe UI", Font.PLAIN, 9));
facilLbl.setForeground(Color.GRAY);
facilLbl.setBounds(8, 38, 450, 14);   
card.add(facilLbl);

        return card;
    }

    // ── Current allocation table ──────────────────────────────────────────────
    private void loadAllocationTable() {
        List<Object[]> allocs = roomDao.getAllAllocations();
        String[] cols = {"Student", "Room", "Type", "Date Assigned"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : allocs) model.addRow(row);

        view.getAllocationTable().setModel(model);
        view.getAllocationTable().setRowHeight(30);
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}
