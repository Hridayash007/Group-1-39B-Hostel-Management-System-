package dao;

import database.MYSqlConnector;
import model.ComplaintData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDao {

    private final MYSqlConnector mysql = new MYSqlConnector();

    /** File a new complaint. Returns true on success. */
    public boolean createComplaint(ComplaintData c) {
        String sql = "INSERT INTO complaints (user_id, title, description, category, priority, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'Pending')";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,    c.getUserId());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getDescription());
            ps.setString(4, c.getCategory());
            ps.setString(5, c.getPriority());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /** Fetch complaints for a specific student. */
    public List<ComplaintData> getComplaintsByUser(int userId) {
        return fetchWhere("WHERE c.user_id = " + userId + " ORDER BY c.date_filed DESC");
    }

    /** Fetch ALL complaints (admin view) with username joined. */
    public List<ComplaintData> getAllComplaints() {
        return fetchWhere("ORDER BY c.date_filed DESC");
    }

    /** Fetch complaints filtered by status (admin). */
    public List<ComplaintData> getComplaintsByStatus(String status) {
        return fetchWhere("WHERE c.status = '" + status + "' ORDER BY c.date_filed DESC");
    }

    /** Update status (admin resolves/rejects). */
    public boolean updateStatus(int complaintId, String newStatus) {
        String sql = "UPDATE complaints SET status = ? WHERE complaint_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /** Delete a complaint. */
    public boolean deleteComplaint(int complaintId) {
        String sql = "DELETE FROM complaints WHERE complaint_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /** Count helpers for stats panels. */
    public int countByUser(int userId)               { return countWhere("user_id = " + userId); }
    public int countByUserAndStatus(int uid, String s){ return countWhere("user_id = " + uid + " AND status = '" + s + "'"); }
    public int countAll()                             { return countWhere("1=1"); }
    public int countByStatus(String status)           { return countWhere("status = '" + status + "'"); }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private List<ComplaintData> fetchWhere(String whereClause) {
        List<ComplaintData> list = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM complaints c "
                   + "JOIN users u ON c.user_id = u.user_id " + whereClause;
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    private int countWhere(String condition) {
        String sql = "SELECT COUNT(*) FROM complaints WHERE " + condition;
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    private ComplaintData mapRow(ResultSet rs) throws SQLException {
        ComplaintData c = new ComplaintData();
        c.setComplaintId(rs.getInt("complaint_id"));
        c.setUserId(     rs.getInt("user_id"));
        c.setTitle(      rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(   rs.getString("category"));
        c.setPriority(   rs.getString("priority"));
        c.setStatus(     rs.getString("status"));
        try { c.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("date_filed");
        if (ts != null) c.setDateFiled(ts.toLocalDateTime());
        return c;
    }
}
