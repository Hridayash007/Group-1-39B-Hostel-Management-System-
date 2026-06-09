package controller;

import dao.ComplaintDao;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.ComplaintData;
import model.UserData;
import view.IssueComplaints;
import view.LogIn;
import view.StudentDashboard;
import view.StudentProfile;
import view.ViewComplaintExpand;
import view.ViewNotice;
import view.issueComplaintExpand;

public class IssueComplaintsController {

    private final ComplaintDao dao = new ComplaintDao();
    private final IssueComplaints view;
    private final UserData user;
    private String currentFilter = "All";

    public IssueComplaintsController(IssueComplaints view, UserData user) {
        this.view = view;
        this.user = user;

        view.setWelcomeUser(user.getUsername());
        refresh();

        // ── File New Complaint ───────────────────────────────────────────────
        view.NewComplaintListener(e -> {
            issueComplaintExpand form = new issueComplaintExpand();
            new IssueComplaintExpandController(form, user, this).open(view);
        });

        // ── Filter buttons ───────────────────────────────────────────────────
        view.AllListener     (e -> { currentFilter = "All";      refresh(); });
        view.PendingListener (e -> { currentFilter = "Pending";  refresh(); });
        view.ResolvedListener(e -> { currentFilter = "Resolved"; refresh(); });

        // ── Navigation ───────────────────────────────────────────────────────
        view.DashboardListener(e -> {
            close();
            new StudentDashboardController(new StudentDashboard(), user).open();
        });
        view.NoticeListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        view.MyProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
        });
        view.ProfileListener(e -> {
            close();
            new StudentProfileController(new StudentProfile(), user).open();
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

    /** Reload stats + complaint cards. Called after submit or filter change. */
    public void refresh() {
        view.setTotalComplaints(dao.countByUser(user.getId()));
        view.setPendingCount   (dao.countByUserAndStatus(user.getId(), "Pending"));
        view.setResolvedCount  (dao.countByUserAndStatus(user.getId(), "Resolved"));

        List<ComplaintData> list = currentFilter.equals("All")
                ? dao.getComplaintsByUser(user.getId())
                : dao.getComplaintsByStatus(currentFilter).stream()
                      .filter(c -> c.getUserId() == user.getId())
                      .toList();

        loadCards(list);
    }

    // ── Card builder ──────────────────────────────────────────────────────────
    private void loadCards(List<ComplaintData> list) {
        JPanel container = view.getComplaintPanel();
        container.removeAll();

        int y = 10;
        if (list.isEmpty()) {
            JLabel empty = new JLabel("No complaints found.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(Color.GRAY);
            empty.setBounds(20, y, 400, 20);
            container.add(empty);
            y += 30;
        } else {
            for (ComplaintData c : list) {
                JPanel card = buildCard(c);
                card.setBounds(10, y, 1130, 90);
                container.add(card);
                y += 100;
            }
        }

        container.setPreferredSize(new java.awt.Dimension(1150, Math.max(y + 10, 100)));
        container.revalidate();
        container.repaint();
    }

    private JPanel buildCard(ComplaintData c) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235)));

        Color statusColor = switch (c.getStatus()) {
            case "Resolved" -> new Color(220, 252, 231);
            case "Rejected" -> new Color(254, 226, 226);
            default         -> new Color(255, 237, 213);
        };

        // Title
        JLabel titleLbl = new JLabel(c.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setBounds(10, 10, 800, 18);
        card.add(titleLbl);

        // Description preview
        String desc = c.getDescription().length() > 120
                ? c.getDescription().substring(0, 120) + "..." : c.getDescription();
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLbl.setForeground(new Color(75, 85, 99));
        descLbl.setBounds(10, 32, 880, 16);
        card.add(descLbl);

        // Date
        JLabel dateLbl = new JLabel(c.getFormattedDate());
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateLbl.setForeground(Color.GRAY);
        dateLbl.setBounds(10, 58, 110, 14);
        card.add(dateLbl);

        // Category
        JLabel catLbl = new JLabel(c.getCategory());
        catLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        catLbl.setForeground(new Color(59, 130, 246));
        catLbl.setBounds(130, 58, 80, 14);
        card.add(catLbl);

        // Priority
        JLabel priLbl = new JLabel(c.getPriority());
        priLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        priLbl.setForeground(new Color(107, 114, 128));
        priLbl.setBounds(220, 58, 60, 14);
        card.add(priLbl);

        // Status badge
        JPanel statusBadge = new JPanel();
        statusBadge.setBackground(statusColor);
        statusBadge.setLayout(null);
        statusBadge.setBounds(290, 52, 75, 20);
        JLabel statusLbl = new JLabel(c.getStatus());
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        statusLbl.setBounds(5, 3, 65, 14);
        statusBadge.add(statusLbl);
        card.add(statusBadge);

        // ── VIEW button ───────────────────────────────────────────────────────
               JButton viewBtn = new JButton();
        try {
            viewBtn.setIcon(new ImageIcon(getClass().getResource("/images/viewnotice.png")));
        } catch (Exception ex) {
            viewBtn.setText("👁");
        }
        viewBtn.setForeground(new Color(220, 38, 38));
        viewBtn.setToolTipText("View full complaint");
        viewBtn.setBorderPainted(false);
        viewBtn.setContentAreaFilled(false);
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewBtn.setBounds(1010, 28, 50, 32);

        card.add(viewBtn);

        viewBtn.addActionListener(e -> openExpandView(c));

        // ── DELETE button ─────────────────────────────────────────────────────
        JButton deleteBtn = new JButton();
        try {
            deleteBtn.setIcon(new ImageIcon(getClass().getResource("/images/deletenotice.png")));
        } catch (Exception ex) {
            deleteBtn.setText("🗑");
        }
        deleteBtn.setForeground(new Color(220, 38, 38));
        deleteBtn.setToolTipText("Delete complaint");
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteBtn.setBounds(1060, 28, 50, 32);
        card.add(deleteBtn);

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Delete complaint: \"" + c.getTitle() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dao.deleteComplaint(c.getComplaintId())) {
                    JOptionPane.showMessageDialog(view, "Complaint deleted.");
                    refresh();
                }
            }
        });

        return card;
    }

    /** Opens ViewComplaintExpand as a modal dialog. */
    private void openExpandView(ComplaintData c) {
        ViewComplaintExpand expandView = new ViewComplaintExpand();
        ViewComplaintExpandController ctrl = new ViewComplaintExpandController(expandView);
        ctrl.setComplaint(c);
        ctrl.open(view);
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}