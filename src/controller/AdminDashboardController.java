package controller;

import dao.ComplaintDao;
import dao.NoticeDao;
import dao.UserDao;
import javax.swing.JOptionPane;
import view.AdminDasboard;
import view.LogIn;
import view.NoticeAdmin;
import view.ViewComplaint;
import view.ViewStudentDetails;
import view.AdminMenuAdjustment;
import view.ViewNotice;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.List;
import dao.RoomDao;
import model.UserData;
import model.ComplaintData;
import model.RoomData;
public class AdminDashboardController {

    private final UserDao      userDao      = new UserDao();
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final NoticeDao    noticeDao    = new NoticeDao();
    private final AdminDasboard view;

    public AdminDashboardController(AdminDasboard view) {
        this.view = view;

        loadDashboard();
        wireNavigation();
    }

    // ── Load live stats into the four stat cards ──────────────────────────────
   private void loadDashboard(){


    // Total Students

    int students =
            userDao.getAllStudents().size();


    view.setTotalStudents(students);


    // Complaints

    int complaints =
            complaintDao.countAll();


    view.setTotalComplaints(complaints);



    // Notices

    int notices =
            noticeDao.countAll();


    view.setActiveNotices(notices);



    // Rooms

    loadRoomDetails();


    // Recent students

    loadRecentStudents();


    // Recent complaints

    loadRecentComplaints();

}

    // ── Wire every sidebar button ─────────────────────────────────────────────
    private void wireNavigation() {

        // Dashboard — already here, just reload stats
        view.DashboardListener(e -> loadDashboard());

        // Students
        view.StudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new ViewStudentDetails()).open();
        });

        // Complaints
        view.ComplaintsListener(e -> {
            close();
            new ViewComplaintController(new ViewComplaint()).open();
        });

        // Notice
        view.NoticeListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });

        // "View all" urgent complaints → opens ViewComplaint
        view.ViewAllComplaintsListener(e -> {
            close();
            new ViewComplaintController(new ViewComplaint()).open();
        });

        // "View all" recent check-ins → opens ViewStudentDetails
        view.ViewAllStudentsListener(e -> {
            close();
            new ViewStudentDetailsController(new ViewStudentDetails()).open();
        });
        
        //meal routine
        view.MealRoutineListener(e -> {
            close();
            new AdminMenuAdjustmentController(new AdminMenuAdjustment()).open();
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
            new RoomAllocationController(new view.RoomAllocation1()).open();
        });
        
        // top right Notice
        view.NotificationListener(e -> {
            close();
            new NoticeAdminController(new NoticeAdmin()).open();
        });
        
        
        view.PaymentDetailsListener(e -> {
            close();
            new ViewPaymentDetailsAdminController(new view.ViewPaymentDetailsAdmin()).open();
        });
        // Sign Out
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
    
    private void loadRoomDetails(){
    RoomDao dao = new RoomDao();
    int occupied =
            dao.countOccupiedBeds();
    int total =
            dao.countTotalCapacity();
    view.setRoomOccupied(
            occupied,
            total
    );
}
    
private void loadRecentStudents(){
    view.removeStaticCheckInPlaceholder();
    RoomDao roomDao = new RoomDao();
    List<UserData> students =
            userDao.getAllStudents();
    int y = 50;
    for(UserData u : students){
        JPanel panel = new JPanel();
        panel.setLayout(null);
        JLabel name =
        new JLabel(u.getUsername());
        String room="Not Allocated";
        RoomData r =
        roomDao.getRoomByUser(
            u.getId()
        );
        if(r != null){
            room = r.getRoomNumber();
        }
        JLabel info =
        new JLabel(
        "ID: "+u.getId()
        +" Room: "+room
        );
        panel.add(name);
        panel.add(info);
        name.setBounds(20,10,200,20);
        info.setBounds(20,30,250,20);
        view.getRecentCheckInPanel()
            .add(panel);
        panel.setBounds(10,y,560,60);
        y+=70;
    }
    view.getRecentCheckInPanel()
        .revalidate();
    view.getRecentCheckInPanel()
        .repaint();

}
    
    
    
    private void RecentStudents(){
    view.removeStaticCheckInPlaceholder();
    List<UserData> students =
            userDao.getAllStudents();
    int y = 50;
    for(UserData u : students){
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        JLabel name =
        new JLabel(u.getFullName());
        JLabel info =
        new JLabel(
        "ID: "+u.getId()
        );
        panel.add(name);
        panel.add(info);
        name.setBounds(20,10,200,20);
        info.setBounds(20,30,300,20);
        view.getCheckInsContainer()
            .add(panel);
        panel.setBounds(10,y,560,60);
        y+=70;
        if(y>350)
            break;
    }
    view.getCheckInsContainer()
        .revalidate();
    view.getCheckInsContainer()
        .repaint();

}
    private void loadRecentComplaints(){
    view.removeStaticComplaintPlaceholders();
    List<ComplaintData> list =
            complaintDao.getAllComplaints();
    int y = 60;
    for(ComplaintData c : list){
        JPanel panel =
                new JPanel();
        panel.setLayout(null);
        panel.setBackground(
            new Color(255,245,220)
        );
        JLabel id =
        new JLabel(
            "C-"+c.getComplaintId()
        );
        JLabel title =
        new JLabel(
            c.getDescription()
        );
        panel.add(id);
        panel.add(title);
        id.setBounds(
            20,10,100,20
        );
        title.setBounds(
            20,35,350,20
        );
        view.getComplaintsContainer()
            .add(panel);
        panel.setBounds(30,y,530,70);
        y+=85;
        if(y>300)
            break;
    }

    view.getComplaintsContainer()
        .revalidate();
    view.getComplaintsContainer()
        .repaint();

}
    
    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}
