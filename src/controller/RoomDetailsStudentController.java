package controller;

import dao.RoomDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import model.RoomData;
import model.UserData;
import view.IssueComplaints;
import view.MakePayment;
import view.RoomDetailsStudent;
import view.StudentDashboard;
import view.StudentProfile;
import view.ViewNotice;


public class RoomDetailsStudentController {

    private final RoomDao roomDao = new RoomDao();
    private final RoomDetailsStudent view;
    private final UserData user;   // logged-in student passed from login/session

    // Facility keywords matched (case-insensitive) against the facilities string
    // stored in the rooms table, e.g. "WiFi, AC, Water Supply"
    private static final String KW_WIFI  = "wifi";
    private static final String KW_AC    = "ac";
    private static final String KW_POWER = "power";
    private static final String KW_WATER = "water";

    public RoomDetailsStudentController(RoomDetailsStudent view, UserData currentUser) {
        this.view = view;
        this.user = currentUser;
        loadRoomDetails();
        wireNavigation();
    }

    // ── Main data-loading method ──────────────────────────────────────────────

    private void loadRoomDetails() {
        // Fetch the room allocated to this student (null if none)
        RoomData room = roomDao.getRoomByUser(user.getId());

        if (room == null) {
            // Student has no room yet — show the "no allocation" state
            view.showNoRoomAllocated();
            return;
        }

        // ── Top banner ────────────────────────────────────────────────────────
        view.setRoomNumber(room.getRoomNumber());
        view.setBlockAndFloor(room.getBlock(), room.getFloor());

        String checkIn = roomDao.getCheckInDate(user.getId());
        view.setAllocationDate(checkIn.isEmpty() ? "—" : checkIn);

        // ── Details panel ─────────────────────────────────────────────────────
        view.setRoomType(room.getType());
        view.setFloorNo(room.getFloor());
        view.setBlockVariable(room.getBlock());
        view.setCheckinDate(checkIn.isEmpty() ? "—" : checkIn);

        // ── Roommates panel ───────────────────────────────────────────────────
        List<UserData> roommates = roomDao.getRoommatesForRoom(
                room.getRoomId(), user.getId());

        List<String[]> roommateRows = new ArrayList<>();
        for (UserData rm : roommates) {
            String displayName = (rm.getFullName() != null && !rm.getFullName().isBlank())
                    ? rm.getFullName()
                    : rm.getUsername();
            String subtitle = "#" + rm.getId()
                    + (rm.getProgram() != null && !rm.getProgram().isBlank()
                       ? "  ·  " + rm.getProgram() : "");
            roommateRows.add(new String[]{displayName, subtitle});
        }
        view.setRoommates(roommateRows);

        // ── Facilities panel ──────────────────────────────────────────────────
        String facilities = room.getFacilities() == null
        ? ""
        : room.getFacilities()
              .toLowerCase()
              .replace("-", "")
              .replace(" ", "");

        // status indices: 1=WiFi, 2=AC, 3=Power, 4=Water Supply  (matches the
        // order in the .form: wifipanel→status1, acpanel→status2,
        //                      powerpanel→status3, watersupplypanel→status4)
        view.setFacilityStatus(1, facilities.contains(KW_WIFI));
        view.setFacilityStatus(2, facilities.contains(KW_AC));
        view.setFacilityStatus(3, facilities.contains(KW_POWER));
        view.setFacilityStatus(4, facilities.contains(KW_WATER));
    }

    // ── Navigation wiring ─────────────────────────────────────────────────────

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

        // Room Details — already on this screen; just refresh
        view.RoomDetailsListener(e -> loadRoomDetails());

        //--Make Payment
        view.MakePaymentListener(e -> {
            close();
            new MakePaymentController(new MakePayment(), user).open();
        });

        // Sign Out (Dashboard1 button in the view)
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

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}