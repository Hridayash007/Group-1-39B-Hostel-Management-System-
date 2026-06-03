package dao;

import database.MYSqlConnector;
import model.NoticeData;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoticeDao {

    private final MYSqlConnector mysql = new MYSqlConnector();

    /** Insert a new notice. Returns true on success. */
    public boolean createNotice(NoticeData notice) {
        String sql = "INSERT INTO notices (title, description, category, priority, is_pinned) "
                   + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getDescription());
            ps.setString(3, notice.getCategory());
            ps.setString(4, notice.getPriority());
            ps.setInt(5,    notice.isPinned() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /** Fetch ALL notices ordered newest first. */
    public List<NoticeData> getAllNotices() {
        List<NoticeData> list = new ArrayList<>();
        String sql = "SELECT * FROM notices ORDER BY date_issued DESC";
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

    /** Fetch only PINNED notices. */
    public List<NoticeData> getPinnedNotices() {
        List<NoticeData> list = new ArrayList<>();
        String sql = "SELECT * FROM notices WHERE is_pinned = 1 ORDER BY date_issued DESC";
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

    /** Delete a notice by ID. */
    public boolean deleteNotice(int noticeId) {
        String sql = "DELETE FROM notices WHERE notice_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noticeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /** Count totals for admin dashboard stats. */
    public int countAll()    { return countWhere("1=1"); }
    public int countPinned() { return countWhere("is_pinned = 1"); }
    public int countUrgent() { return countWhere("priority = 'Urgent'"); }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private int countWhere(String condition) {
        String sql = "SELECT COUNT(*) FROM notices WHERE " + condition;
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

    private NoticeData mapRow(ResultSet rs) throws SQLException {
        NoticeData n = new NoticeData();
        n.setNoticeId(  rs.getInt("notice_id"));
        n.setTitle(      rs.getString("title"));
        n.setDescription(rs.getString("description"));
        n.setCategory(   rs.getString("category"));
        n.setPriority(   rs.getString("priority"));
        n.setPinned(     rs.getInt("is_pinned") == 1);
        Timestamp ts = rs.getTimestamp("date_issued");
        if (ts != null) n.setDateIssued(ts.toLocalDateTime());
        return n;
    }
}