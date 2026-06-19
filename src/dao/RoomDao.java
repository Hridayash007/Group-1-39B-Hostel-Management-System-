package dao;

import database.MYSqlConnector;
import model.RoomData;
import model.UserData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    private final MYSqlConnector mysql = new MYSqlConnector();

    // ── Room CRUD ─────────────────────────────────────────────────────────────

    public boolean addRoom(RoomData r) {
        String sql = "INSERT INTO rooms(room_number, block, floor, type, capacity, facilities, fee)VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getBlock());
            ps.setString(3, r.getFloor());
            ps.setString(4, r.getType());
            ps.setInt(5,    r.getCapacity());
            ps.setString(6, r.getFacilities());
            ps.setDouble(7, r.getFee());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public List<RoomData> getAllRooms() {
        return fetchRoomsWhere("ORDER BY r.block, r.room_number");
    }

    public List<RoomData> getRoomsByBlock(String block) {
        return fetchRoomsWhere("WHERE r.block = '" + block + "' ORDER BY r.room_number");
    }

    public List<RoomData> getVacantRooms() {
    return fetchRoomsWhere(
        "WHERE r.occupied < r.capacity ORDER BY r.block, r.room_number"
    );
}

    // ── Room Allocation ───────────────────────────────────────────────────────

    /** Allocate a student to a room. Updates occupied count and status. */
    public boolean allocateRoom(int roomId, int userId) {
        Connection conn = mysql.openConnection();
        try {
            conn.setAutoCommit(false);
            
            //check
            String checkSql = "SELECT occupied, capacity FROM rooms WHERE room_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, roomId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int occupied = rs.getInt("occupied");
                    int capacity = rs.getInt("capacity");

                    if (occupied >= capacity) {
                        conn.rollback();
                        return false; // room is full
                    }
                }
            }
            
            // Insert allocation
            String ins = "INSERT INTO room_allocations (room_id, user_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                ps.setInt(1, roomId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // Increment occupied and update status
            // Update occupied first, then derive status from the NEW occupied value
            // BUG FIX: CASE must compare occupied+1 (the new value), not the old occupied.
            // Using the old value means a room that just became full is still marked 'Partial'.
            String upd =
                "UPDATE rooms SET " +
                "occupied = occupied + 1, " +
                "status = CASE " +
                "WHEN occupied  >= capacity THEN 'Full' " +
                "WHEN occupied  > 0         THEN 'Partial' " +
                "ELSE 'Vacant' END " +
                "WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setInt(1, roomId);
                ps.executeUpdate();
            }
            
          
            String feeSql =
            "INSERT INTO fees(user_id, room_id, student_name, room_number, amount, fee_month, status) "
            +
            "SELECT u.user_id, r.room_id, COALESCE(NULLIF(u.full_name,''), u.username), r.room_number, r.fee, "
            +
            "DATE_FORMAT(NOW(),'%M %Y'), 'Pending' "
            +
            "FROM users u, rooms r "
            +
            "WHERE u.user_id=? AND r.room_id=?";


            try(PreparedStatement ps = conn.prepareStatement(feeSql)){

                ps.setInt(1, userId);
                ps.setInt(2, roomId);

                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            mysql.closeConnection(conn);
        }
    }

    /** Remove allocation (de-allocate student). */
    public boolean deallocateRoom(int userId) {
        Connection conn = mysql.openConnection();
        try {
            conn.setAutoCommit(false);

            // Get room_id first
            int roomId = -1;
            String sel = "SELECT room_id FROM room_allocations WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sel)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) roomId = rs.getInt("room_id");
            }
            if (roomId == -1) return false;

            // Delete allocation
            String del = "DELETE FROM room_allocations WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(del)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            
            String upd =
                "UPDATE rooms r2 "
                + "JOIN (SELECT GREATEST(occupied - 1, 0) AS new_occ, capacity, room_id "
                + "      FROM rooms WHERE room_id = ?) sub ON r2.room_id = sub.room_id "
                + "SET r2.occupied = sub.new_occ, "
                + "    r2.status = CASE "
                + "        WHEN sub.new_occ = 0               THEN 'Vacant' "
                + "        WHEN sub.new_occ >= sub.capacity   THEN 'Full' "
                + "        ELSE 'Partial' END";
            try (PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setInt(1, roomId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            mysql.closeConnection(conn);
        }
    }

    /** Get room allocated to a specific student (null if none). */
    public RoomData getRoomByUser(int userId) {
        String sql = "SELECT r.* FROM rooms r "
                   + "JOIN room_allocations ra ON r.room_id = ra.room_id "
                   + "WHERE ra.user_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRoom(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    /** Students who have NO room allocated yet. */
    public List<UserData> getUnallocatedStudents() {
        List<UserData> list = new ArrayList<>();
        String sql = "SELECT u.* FROM users u "
                   + "WHERE u.user_id NOT IN (SELECT user_id FROM room_allocations) "
                   + "ORDER BY u.user_id";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserData u = new UserData();
                u.setId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setFullName(safe(rs, "full_name"));
                u.setProgram(safe(rs, "program"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    /** All current allocations with student + room info. */
    public List<Object[]> getAllAllocations() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.full_name, "
                   + "r.room_number, r.block, r.type, ra.date_assigned "
                   + "FROM room_allocations ra "
                   + "JOIN users u ON ra.user_id = u.user_id "
                   + "JOIN rooms r ON ra.room_id = r.room_id "
                   + "ORDER BY ra.date_assigned DESC";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("full_name");
                if (name == null || name.isEmpty()) name = rs.getString("username");
                Timestamp ts = rs.getTimestamp("date_assigned");
                String date = ts != null ? ts.toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
                list.add(new Object[]{
                    name,
                    rs.getString("room_number") + " (" + rs.getString("block") + ")",
                    rs.getString("type"),
                    date
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────
    public int countTotalRooms()      { return countWhere("1=1"); }
    public int countVacantRooms()     { return countWhere("status = 'Vacant'"); }
    public int countAllocations() {
        String sql = "SELECT COUNT(*) FROM room_allocations";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.closeConnection(conn); }
        return 0;
    }
    public int countUnallocatedStudents() {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id NOT IN (SELECT user_id FROM room_allocations)";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.closeConnection(conn); }
        return 0;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private List<RoomData> fetchRoomsWhere(String clause) {
        List<RoomData> list = new ArrayList<>();
        String sql = "SELECT r.* FROM rooms r " + clause;
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRoom(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    private int countWhere(String condition) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE " + condition;
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.closeConnection(conn); }
        return 0;
    }

    private RoomData mapRoom(ResultSet rs) throws SQLException {
    RoomData r = new RoomData();

    r.setRoomId(rs.getInt("room_id"));
    r.setRoomNumber(rs.getString("room_number"));
    r.setBlock(rs.getString("block"));
    r.setFloor(rs.getString("floor"));
    r.setType(rs.getString("type"));
    r.setCapacity(rs.getInt("capacity"));
    r.setOccupied(rs.getInt("occupied"));
    r.setFacilities(safe(rs, "facilities"));
    r.setStatus(rs.getString("status"));
    r.setFee(rs.getDouble("fee"));

    return r;
}

    private String safe(ResultSet rs, String col) {
        try { String v = rs.getString(col); return v != null ? v : ""; }
        catch (SQLException e) { return ""; }
    }
    
    
   public int countTotalCapacity() {
    String sql = "SELECT COALESCE(SUM(capacity),0) FROM rooms";

    Connection conn = mysql.openConnection();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        mysql.closeConnection(conn);
    }

    return 0;
}

public int countOccupiedBeds() {
    String sql = "SELECT COALESCE(SUM(occupied),0) FROM rooms";

    Connection conn = mysql.openConnection();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        mysql.closeConnection(conn);
    }

    return 0;
}

public int countRoomsWithVacantBeds() {
    String sql = "SELECT COUNT(*) FROM rooms WHERE occupied < capacity";

    Connection conn = mysql.openConnection();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        mysql.closeConnection(conn);
    }
    return 0;
}

    /**
     * Returns all students sharing the same room as the given user,
     * excluding the user themselves.
     */
    public List<UserData> getRoommatesForRoom(int roomId, int excludeUserId) {
        List<UserData> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.full_name, u.program "
                   + "FROM users u "
                   + "JOIN room_allocations ra ON u.user_id = ra.user_id "
                   + "WHERE ra.room_id = ? AND u.user_id != ? "
                   + "ORDER BY u.user_id";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, excludeUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserData u = new UserData();
                u.setId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(safe(rs, "full_name"));
                u.setProgram(safe(rs, "program"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    /**
     * Returns the check-in date (date_assigned) for a student's allocation,
     * formatted as "MMM dd, yyyy". Returns empty string if not found.
     */
    public String getCheckInDate(int userId) {
        String sql = "SELECT date_assigned FROM room_allocations WHERE user_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("date_assigned");
                if (ts != null) {
                    return ts.toLocalDateTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return "";
    }
}