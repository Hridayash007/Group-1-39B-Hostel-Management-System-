package controller;

import dao.NoticeDao;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.NoticeData;
import view.IssueNotice;
import view.LogIn;
import view.NoticeAdmin;
import view.ViewStudentDetails;
import view.viewnoticeexpand;
import view.AdminDasboard;
import view.AdminMenuAdjustment;
        
public class NoticeAdminController {

    private final NoticeDao noticeDao = new NoticeDao();
    private final NoticeAdmin view;

    public NoticeAdminController(NoticeAdmin view) {
        this.view = view;

        loadStats();
        loadNoticeList();

        // ── New Notice button ────────────────────────────────────────────────
        view.NewNoticeListener(e -> {
            IssueNotice issueView = new IssueNotice();
            new IssueNoticeController(issueView, this).open();
        });

        // ── Room Details button ─────────────────────────────────────────────────
        view.RoomDetailsListener(e -> {
            close();
            new RoomDetailsController(new view.RoomDetails()).open();
        }
);
         // --Dashboard   
         view.DashboardListener(e -> {
            close();
            new AdminDashboardController(new AdminDasboard()).open();
        });
         
         
        // ── Room Allocation button ─────────────────────────────────────────────
        view.RoomAllocationListener(e -> {
            close();
            new RoomAllocationController(new view.RoomAllocation1()).open();
        });

        // ── Complaints button (sidebar) ───────────────────────────────────────
        view.ComplaintsListener(e -> {
            close();
            new ViewComplaintController(new view.ViewComplaint()).open();
        });

        // ── Students button (sidebar) ────────────────────────────────────────
        view.StudentsListener(e -> {
            close();
            ViewStudentDetails studView = new ViewStudentDetails();
            new ViewStudentDetailsController(studView).open();
        });
        
        //Payment Details
        view.PaymentDetailsListener(e -> {
            close();
            new ViewPaymentDetailsAdminController(new view.ViewPaymentDetailsAdmin()).open();
        });
        
         //meal routine
        view.MealRoutineListener(e -> {
            close();
            new AdminMenuAdjustmentController(new AdminMenuAdjustment()).open();
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
    
       

    /** Called by IssueNoticeController after a notice is published. */
    public void refresh() {
        loadStats();
        loadNoticeList();
    }

    /** Returns the view — used by IssueNoticeController to center dialogs. */
    public NoticeAdmin getView() { return view; }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void loadStats() {
        view.setTotalNotices(noticeDao.countAll());
        view.setPinnedNotices(noticeDao.countPinned());
        view.setUrgentNotices(noticeDao.countUrgent());
    }

    private void loadNoticeList() {
        List<NoticeData> notices = noticeDao.getAllNotices();
        JPanel container = view.getNoticeListPanel();
        container.removeAll();

        int y = 10;
        for (NoticeData n : notices) {
            JPanel card = buildAdminNoticeCard(n);
            card.setBounds(10, y, 1150, 90);
            container.add(card);
            y += 100;
        }

        container.setPreferredSize(new java.awt.Dimension(1160, Math.max(y + 10, 100)));
        container.revalidate();
        container.repaint();
    }

    // Builds a notice card with View and Delete icon buttons on the right.
   
    private JPanel buildAdminNoticeCard(NoticeData n) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235)));

        Color badgeColor = switch (n.getPriority()) {
            case "Urgent" -> new Color(254, 226, 226);
            case "High"   -> new Color(255, 237, 213);
            default       -> new Color(220, 252, 231);
        };

        // ID
        JLabel idLabel = new JLabel("ID: " + n.getNoticeId());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        idLabel.setForeground(Color.GRAY);
        idLabel.setBounds(10, 5, 80, 14);
        card.add(idLabel);

        // Title
        JLabel titleLabel = new JLabel(n.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setBounds(10, 20, 980, 18);
        card.add(titleLabel);

        // Description preview
        String desc = n.getDescription().length() > 120
                ? n.getDescription().substring(0, 120) + "..." : n.getDescription();
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(75, 85, 99));
        descLabel.setBounds(10, 42, 980, 16);
        card.add(descLabel);

        // Date
        JLabel dateLabel = new JLabel(n.getFormattedDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setBounds(10, 64, 100, 14);
        card.add(dateLabel);

        // Category
        JLabel catLabel = new JLabel(n.getCategory());
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        catLabel.setForeground(new Color(59, 130, 246));
        catLabel.setBounds(120, 64, 80, 14);
        card.add(catLabel);

        // Priority badge
        JPanel priorityBadge = new JPanel();
        priorityBadge.setBackground(badgeColor);
        priorityBadge.setLayout(null);
        priorityBadge.setBounds(210, 60, 60, 20);
        JLabel priLabel = new JLabel(n.getPriority());
        priLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        priLabel.setBounds(5, 3, 55, 14);
        priorityBadge.add(priLabel);
        card.add(priorityBadge);

        // Pinned
        if (n.isPinned()) {
            JLabel pinnedLabel = new JLabel("📌 Pinned");
            pinnedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            pinnedLabel.setForeground(new Color(99, 102, 241));
            pinnedLabel.setBounds(285, 64, 80, 14);
            card.add(pinnedLabel);
        }

        // ── View button — icon with text fallback ─────────────────────────────
        JButton viewBtn = new JButton();
        try {
            viewBtn.setIcon(new ImageIcon(getClass().getResource("/images/viewnotice.png")));
        } catch (Exception ex) {
            viewBtn.setText("👁");
        }
        viewBtn.setToolTipText("View full notice");
        viewBtn.setBorderPainted(false);
        viewBtn.setContentAreaFilled(false);
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewBtn.setBounds(1055, 25, 40, 36);
        card.add(viewBtn);

        viewBtn.addActionListener(e -> {
            viewnoticeexpand expandView = new viewnoticeexpand();
            ViewNoticeExpandController ctrl = new ViewNoticeExpandController(expandView);
            ctrl.setNotice(n);
            ctrl.open(view);
        });

        // ── Delete button — icon with text fallback ───────────────────────────
        JButton deleteBtn = new JButton();
        try {
            deleteBtn.setIcon(new ImageIcon(getClass().getResource("/images/deletenotice.png")));
        } catch (Exception ex) {
            deleteBtn.setText("🗑");
        }
        deleteBtn.setToolTipText("Delete notice");
        deleteBtn.setForeground(new Color(220, 38, 38));
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteBtn.setBounds(1100, 25, 40, 36);
        card.add(deleteBtn);

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Delete notice: \"" + n.getTitle() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = noticeDao.deleteNotice(n.getNoticeId());
                if (deleted) {
                    JOptionPane.showMessageDialog(view, "Notice deleted successfully.");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(view, "Failed to delete notice.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return card;
    }
}