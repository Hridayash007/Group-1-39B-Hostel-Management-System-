package controller;

import dao.MealDao;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import view.AdminMenuAdjustment;

public class AdminMenuAdjustmentController {

    private static final String[] DAYS  =
        {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final String[] MEALS = {"Breakfast", "Lunch", "Dinner"};

    private static final Color C_BREAKFAST = new Color(194, 65, 12);
    private static final Color C_LUNCH     = new Color(29, 78, 216);
    private static final Color C_DINNER    = new Color(109, 40, 217);

    private final AdminMenuAdjustment view;
    private final MealDao mealDao = new MealDao();

    public AdminMenuAdjustmentController(AdminMenuAdjustment view) {
        this.view = view;
        buildMealPanels();
        wireNavigation();
    }

    // ── Build all 7 day panels ────────────────────────────────────────────────
    private void buildMealPanels() {
        JPanel mealContainer = view.getMealContainer();
        mealContainer.removeAll();
        mealContainer.setLayout(new BoxLayout(mealContainer, BoxLayout.Y_AXIS));

        for (String day : DAYS) {
            mealContainer.add(buildDayPanel(day));
            mealContainer.add(Box.createVerticalStrut(10));
        }

        mealContainer.revalidate();
        mealContainer.repaint();
    }

    private JPanel buildDayPanel(String day) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel dayLabel = new JLabel(day);
        dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dayLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(dayLabel, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);

        for (String mealType : MEALS) {
            row.add(buildMealSlot(day, mealType));
        }

        card.add(row, BorderLayout.CENTER);
        return card;
    }

   
    private JPanel buildMealSlot(String day, String mealType) {
        Color accent = accentFor(mealType);
        CardLayout cl = new CardLayout();
        JPanel slot = new JPanel(cl);
        slot.setOpaque(false);

        String savedItems = mealDao.getAllMeals().get(day + "_" + mealType);

        // ── VIEW STATE ───────────────────────────────────────────────────────
        JPanel viewState = new JPanel(new BorderLayout(0, 4));
        viewState.setOpaque(false);

        JPanel viewHeader = new JPanel(new BorderLayout());
        viewHeader.setOpaque(false);
        JLabel viewTitle = new JLabel(mealType);
        viewTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewTitle.setForeground(accent);
        viewHeader.add(viewTitle, BorderLayout.WEST);

        JButton editIcon = iconButton("/images/edit.png");
        viewHeader.add(editIcon, BorderLayout.EAST);
        viewState.add(viewHeader, BorderLayout.NORTH);

        JLabel viewText = new JLabel();
        viewText.setVerticalAlignment(SwingConstants.TOP);
        viewText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewText.setText(toHtml(savedItems));
        viewState.add(viewText, BorderLayout.CENTER);

        // ── EDIT STATE (matches screenshot) ─────────────────────────────────
        JPanel editState = new JPanel(new BorderLayout(0, 6));
        editState.setBackground(new Color(255, 247, 237));
        editState.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(253, 186, 116), 1, true),
            new EmptyBorder(8, 10, 8, 10)));

        JPanel editHeader = new JPanel(new BorderLayout());
        editHeader.setOpaque(false);
        JLabel editTitle = new JLabel(mealType);
        editTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editTitle.setForeground(accent);
        editHeader.add(editTitle, BorderLayout.WEST);

        JPanel icons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        icons.setOpaque(false);
        JButton saveBtn  = iconButton("/images/save.png");
        JButton closeBtn = iconButton("/images/close.png");
        icons.add(saveBtn);
        icons.add(closeBtn);
        editHeader.add(icons, BorderLayout.EAST);
        editState.add(editHeader, BorderLayout.NORTH);

        JTextArea ta = new JTextArea(savedItems != null ? savedItems : "");
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setRows(3);
        ta.setBackground(Color.WHITE);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(6, 8, 6, 8)));

        JScrollPane taScroll = new JScrollPane(ta);
        taScroll.setBorder(null);
        taScroll.setOpaque(false);
        taScroll.getViewport().setOpaque(false);
        editState.add(taScroll, BorderLayout.CENTER);

        // ── Wiring ───────────────────────────────────────────────────────────
        editIcon.addActionListener(e -> {
            String current = mealDao.getAllMeals().get(day + "_" + mealType);
            ta.setText(current != null ? current : "");
            cl.show(slot, "edit");
        });

        saveBtn.addActionListener(e -> {
            String text = ta.getText().trim();
            if (text.isEmpty()) {
                mealDao.deleteMeal(day, mealType);
                viewText.setText(toHtml(null));
            } else {
                mealDao.saveMeal(day, mealType, text);
                viewText.setText(toHtml(text));
            }
            cl.show(slot, "view");
        });

        closeBtn.addActionListener(e -> cl.show(slot, "view"));

        slot.add(viewState, "view");
        slot.add(editState, "edit");
        cl.show(slot, "view");

        return slot;
    }

    private String toHtml(String items) {
    if (items == null || items.trim().isEmpty()) {
        return "<html></html>";
    }

    String escaped = items
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");

    String[] meals = escaped.split(",");

    StringBuilder html = new StringBuilder(
        "<html><ul style='margin-top:0px; margin-left:15px;'>"
    );

    for (String meal : meals) {
        if (!meal.trim().isEmpty()) {
            html.append("<li>")
                .append(meal.trim())
                .append("</li>");
        }
    }

    html.append("</ul></html>");

    return html.toString();
}

    
    private JButton iconButton(String iconResourcePath) {
        JButton btn = new JButton();
        java.net.URL url = getClass().getResource(iconResourcePath);
        if (url != null) {
            btn.setIcon(new ImageIcon(url));
        } else {
           
            String fallback = iconResourcePath.contains("edit")  ? "Edit"
                             : iconResourcePath.contains("save")  ? "Save"
                             : iconResourcePath.contains("close") ? "Close"
                             : "?";
            btn.setText(fallback);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        }
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(2, 4, 2, 4));
        Dimension d = new Dimension(28, 24);
        btn.setPreferredSize(d);
        btn.setMinimumSize(d);
        btn.setMaximumSize(d);
        return btn;
    }

    private Color accentFor(String mealType) {
        return switch (mealType) {
            case "Breakfast" -> C_BREAKFAST;
            case "Lunch"     -> C_LUNCH;
            default          -> C_DINNER;
        };
    }

    private void wireNavigation() {
        view.DashboardListener(e -> {
            close();
            new AdminDashboardController(new view.AdminDasboard()).open();
        });
        view.StudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new view.ViewStudentDetails()).open();
        });
        view.ComplaintsListener(e -> {
            close();
            new ViewComplaintController(new view.ViewComplaint()).open();
        });
        view.NoticeListener(e -> {
            close();
            new NoticeAdminController(new view.NoticeAdmin()).open();
        });
        view.RoomDetailsListener(e -> {
            close();
            new RoomDetailsController(new view.RoomDetails()).open();
        });
        view.RoomAllocationListener(e -> {
            close();
            new RoomAllocationController(new view.RoomAllocation1()).open();
        });
        view.PaymentDetailsListener(e -> {
            close();
            new ViewPaymentDetailsAdminController(new view.ViewPaymentDetailsAdmin()).open();
        });
        view.SignOutListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to sign out?", "Sign Out",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                close();
                new LoginController(new view.LogIn()).open();
            }
        });
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}