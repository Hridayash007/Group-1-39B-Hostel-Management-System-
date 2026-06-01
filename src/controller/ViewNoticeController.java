package controller;

import dao.NoticeDao;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.NoticeData;
import model.UserData;
import view.LogIn;
import view.StudentDashboard;
import view.StudentProfile;
import view.ViewNotice;
import view.viewnoticeexpand;

public class ViewNoticeController {

    private final NoticeDao noticeDao = new NoticeDao();
    private final ViewNotice view;
    private final UserData user;

    public ViewNoticeController(ViewNotice view, UserData user) {
        this.view = view;
        this.user = user;

        view.setWelcomeUser(user.getUsername());
        loadPinnedNotices();
        loadAllNotices();

        // ── Navigation ───────────────────────────────────────────────────────
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

        view.NoticeListener(e -> {
            loadPinnedNotices();
            loadAllNotices();
        });

        // ── Sign Out ─────────────────────────────────────────────────────────
        view.SignOutListener(e -> {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to sign out?", "Sign Out",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE);
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                close();
                new LoginController(new LogIn()).open();
            }
        });
    }

    // ── Pinned notices ────────────────────────────────────────────────────────
    private void loadPinnedNotices() {
        List<NoticeData> pinned = noticeDao.getPinnedNotices();
        JPanel container = view.getPinnedNoticePanel();
        container.removeAll();

        int y = 50;
        if (pinned.isEmpty()) {
            JLabel empty = new JLabel("No pinned notices.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            empty.setForeground(Color.GRAY);
            empty.setBounds(10, y, 400, 20);
            container.add(empty);
        } else {
            for (NoticeData n : pinned) {
                JPanel card = buildPinnedCard(n);
                card.setBounds(10, y, 1160, 50);
                container.add(card);
                y += 60;
            }
        }

        container.setPreferredSize(new java.awt.Dimension(1180, Math.max(y + 10, 120)));
        container.revalidate();
        container.repaint();
    }

    // ── All notices ───────────────────────────────────────────────────────────
    private void loadAllNotices() {
        List<NoticeData> all = noticeDao.getAllNotices();
        JPanel container = view.getAllNoticePanel();
        container.removeAll();

        int y = 10;
        if (all.isEmpty()) {
            JLabel empty = new JLabel("No notices have been issued yet.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(Color.GRAY);
            empty.setBounds(20, y, 500, 20);
            container.add(empty);
            y += 30;
        } else {
            for (NoticeData n : all) {
                JPanel card = buildStudentNoticeCard(n);
                card.setBounds(10, y, 1140, 100);
                container.add(card);
                y += 110;
            }
        }

        container.setPreferredSize(new java.awt.Dimension(1160, Math.max(y + 10, 100)));
        container.revalidate();
        container.repaint();
    }

    // ── Pinned card (compact blue, no view button — click anywhere to expand) ─
    private JPanel buildPinnedCard(NoticeData n) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(new Color(191, 219, 254));
        card.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        JLabel title = new JLabel("📌 " + n.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setBounds(10, 5, 900, 18);
        card.add(title);

        JLabel meta = new JLabel(n.getFormattedDate() + "  |  " + n.getCategory() + "  |  " + n.getPriority());
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        meta.setForeground(new Color(30, 64, 175));
        meta.setBounds(10, 28, 500, 14);
        card.add(meta);

        // View button on right side
        JButton viewBtn = new JButton("👁 View");
        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        viewBtn.setForeground(new Color(30, 64, 175));
        viewBtn.setBorderPainted(false);
        viewBtn.setContentAreaFilled(false);
        viewBtn.setFocusPainted(false);
        viewBtn.setBounds(1090, 10, 70, 28);
        card.add(viewBtn);

        viewBtn.addActionListener(e -> openExpandView(n));

        return card;
    }

    /**
     * Student notice card — has a View button on the right.
     * No delete button (students cannot delete notices).
     */
    private JPanel buildStudentNoticeCard(NoticeData n) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235)));

        Color badgeColor = switch (n.getPriority()) {
            case "Urgent" -> new Color(254, 226, 226);
            case "High"   -> new Color(255, 237, 213);
            default       -> new Color(220, 252, 231);
        };

        // Title
        JLabel titleLabel = new JLabel(n.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setBounds(15, 10, 980, 18);
        card.add(titleLabel);

        // Description preview
        String desc = n.getDescription().length() > 130
                ? n.getDescription().substring(0, 130) + "..." : n.getDescription();
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(75, 85, 99));
        descLabel.setBounds(15, 32, 980, 16);
        card.add(descLabel);

        // Date
        JLabel dateLabel = new JLabel(n.getFormattedDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setBounds(15, 58, 110, 14);
        card.add(dateLabel);

        // Category
        JLabel catLabel = new JLabel(n.getCategory());
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        catLabel.setForeground(new Color(59, 130, 246));
        catLabel.setBounds(135, 58, 80, 14);
        card.add(catLabel);

        // Priority badge
        JPanel badge = new JPanel();
        badge.setBackground(badgeColor);
        badge.setLayout(null);
        badge.setBounds(225, 52, 60, 20);
        JLabel priLabel = new JLabel(n.getPriority());
        priLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        priLabel.setBounds(5, 3, 55, 14);
        badge.add(priLabel);
        card.add(badge);

        // Pinned label
        if (n.isPinned()) {
            JLabel pinnedLbl = new JLabel("📌 Pinned");
            pinnedLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            pinnedLbl.setForeground(new Color(99, 102, 241));
            pinnedLbl.setBounds(300, 58, 80, 14);
            card.add(pinnedLbl);
        }

        // ── View button (students only get View, no Delete) ───────────────────
        JButton viewBtn = new JButton("👁 View");
        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        viewBtn.setForeground(new Color(99, 102, 241));
        viewBtn.setToolTipText("Read full notice");
        viewBtn.setBorderPainted(false);
        viewBtn.setContentAreaFilled(false);
        viewBtn.setFocusPainted(false);
        viewBtn.setBounds(1070, 32, 70, 28);
        card.add(viewBtn);

        viewBtn.addActionListener(e -> openExpandView(n));

        return card;
    }

    /** Opens viewnoticeexpand as a modal dialog centered over ViewNotice window. */
    private void openExpandView(NoticeData n) {
        viewnoticeexpand expandView = new viewnoticeexpand();
        ViewNoticeExpandController ctrl = new ViewNoticeExpandController(expandView);
        ctrl.setNotice(n);
        ctrl.open(view);   // center over ViewNotice window
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}