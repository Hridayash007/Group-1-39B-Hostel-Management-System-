package controller;

import dao.MealDao;
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.UserData;
import view.IssueComplaints;
import view.LogIn;
import view.RoomDetailsStudent;
import view.StudentDashboard;
import view.StudentMealRoutine;
import view.StudentProfile;
import view.ViewNotice;
import view.ViewPaymentDetails;

public class StudentMealRoutineController {

    private static final String[] DAYS  =
        {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
    private static final String[] MEALS = {"Breakfast","Lunch","Dinner"};

    private static final Color C_BREAKFAST = new Color(194, 65, 12);
    private static final Color C_LUNCH     = new Color(29, 78, 216);
    private static final Color C_DINNER    = new Color(109, 40, 217);

    private final StudentMealRoutine view;
    private final MealDao mealDao = new MealDao();
    private final UserData user;

    public StudentMealRoutineController(StudentMealRoutine view, UserData user) {
        this.view = view;
        this.user = user;
        
        buildMealPanels();
        wireNavigation();
    }

    private void buildMealPanels() {
        JPanel mealContainer = view.getMealContainer();
        mealContainer.removeAll();
        mealContainer.setLayout(new BoxLayout(mealContainer, BoxLayout.Y_AXIS));

        Map<String, String> data = mealDao.getAllMeals();

        for (String day : DAYS) {
            mealContainer.add(buildDayPanel(day, data));
            mealContainer.add(Box.createVerticalStrut(10));
        }

        mealContainer.revalidate();
        mealContainer.repaint();
    }

    private JPanel buildDayPanel(String day, Map<String, String> data) {
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
            String items = data.get(day + "_" + mealType);
            row.add(buildMealCard(mealType, items)); // blank cream box if items == null
        }

        card.add(row, BorderLayout.CENTER);
        return card;
    }

    
    private JPanel buildMealCard(String mealType, String items) {
        Color accent = accentFor(mealType);

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(false);

        JLabel title = new JLabel(mealType);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(accent);
        card.add(title, BorderLayout.NORTH);

        JPanel box = new JPanel();
        box.setBackground(new Color(255, 247, 237));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(253, 186, 116), 1, true),
            new EmptyBorder(10, 10, 10, 10)));
        box.setLayout(new BorderLayout());

        JLabel itemsLabel = new JLabel(toHtml(items));
        itemsLabel.setVerticalAlignment(SwingConstants.TOP);
        itemsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        box.add(itemsLabel, BorderLayout.CENTER);

        card.add(box, BorderLayout.CENTER);
        return card;
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
        
         //meal routine
        view.MealRoutineListener(e -> {
            close();
            new StudentMealRoutineController(new StudentMealRoutine(), user).open();
        });
        
        view.PaymentHistoryListener(e -> {
            close();
            new ViewPaymentDetailsController(new ViewPaymentDetails(),user).open();
        });
        
        //top right notice
        view.NotificatinListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
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
    
    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}