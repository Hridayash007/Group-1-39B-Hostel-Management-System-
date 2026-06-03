package dao;

import database.MYSqlConnector;
import model.UserData;
import java.sql.*;

public class UserDao {

    MYSqlConnector mysql = new MYSqlConnector();

    // ── Registration ────────────────────────────────────────────────────────
    public void createUser(UserData user) {
        Connection conn = mysql.openConnection();
        String sql = "INSERT INTO users (username, email, password, confirm_password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, user.getUsername());
            pstm.setString(2, user.getEmail());
            pstm.setString(3, user.getPassword());
            pstm.setString(4, user.getConfirmPassword());
            pstm.executeUpdate();
        } catch (Exception e) {
            System.out.print(e);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public boolean checkUser(UserData user) {
        Connection conn = mysql.openConnection();
        String sql = "SELECT * FROM users WHERE email = ? OR username = ?";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, user.getEmail());
            pstm.setString(2, user.getUsername());
            ResultSet result = pstm.executeQuery();
            return result.next();
        } catch (SQLException ex) {
            System.out.print(ex);
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    // ── Login ────────────────────────────────────────────────────────────────
    public UserData loginUser(String email, String password) {
        Connection conn = mysql.openConnection();
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, email);
            pstm.setString(2, password);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return mapFullRow(rs);
            }
        } catch (SQLException ex) {
            System.out.print(ex);
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    // ── Fetch all details by user_id ─────────────────────────────────────────
    public UserData getStudentDetails(int userId) {
        Connection conn = mysql.openConnection();
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapFullRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    // ── Update all student detail fields (user_id stays constant) ───────────
    public boolean updateStudentDetails(UserData user) {
        String sql = "UPDATE users SET "
                + "full_name = ?, phone = ?, date_of_birth = ?, nationality = ?, "
                + "program = ?, year_of_study = ?, semester = ?, address = ?, "
                + "ec_name = ?, ec_relation = ?, ec_number = ?, email = ? "
                + "WHERE user_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  user.getFullName());
            ps.setString(2,  user.getPhone());
            ps.setString(3,  user.getDateOfBirth());
            ps.setString(4,  user.getNationality());
            ps.setString(5,  user.getProgram());
            ps.setString(6,  user.getYearOfStudy());
            ps.setString(7,  user.getSemester());
            ps.setString(8,  user.getAddress());
            ps.setString(9,  user.getEcName());
            ps.setString(10, user.getEcRelation());
            ps.setString(11, user.getEcNumber());
            ps.setString(12, user.getEmail());
            ps.setInt(13,    user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ── Password reset ───────────────────────────────────────────────────────
    public boolean checkEmailExists(String email) {
        Connection conn = mysql.openConnection();
        String query = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public boolean updatePassword(String email, String newPassword, String confirmPassword) {
        Connection conn = mysql.openConnection();
        String query = "UPDATE users SET password = ?, confirm_password = ? WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newPassword);
            ps.setString(2, confirmPassword);
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

   
    private UserData mapFullRow(ResultSet rs) throws SQLException {
        UserData user = new UserData();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));

        // Extended fields — may be NULL for new users
        user.setFullName(    nullSafe(rs, "full_name"));
        user.setPhone(       nullSafe(rs, "phone"));
        user.setDateOfBirth( nullSafe(rs, "date_of_birth"));
        user.setNationality( nullSafe(rs, "nationality"));
        user.setProgram(     nullSafe(rs, "program"));
        user.setYearOfStudy( nullSafe(rs, "year_of_study"));
        user.setSemester(    nullSafe(rs, "semester"));
        user.setAddress(     nullSafe(rs, "address"));
        user.setEcName(      nullSafe(rs, "ec_name"));
        user.setEcRelation(  nullSafe(rs, "ec_relation"));
        user.setEcNumber(    nullSafe(rs, "ec_number"));
        return user;
    }
    
    public java.util.List<model.UserData> getAllStudents() {
    java.util.List<model.UserData> list = new java.util.ArrayList<>();
    String sql = "SELECT * FROM users ORDER BY user_id ASC";
    java.sql.Connection conn = mysql.openConnection();
    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
        java.sql.ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            model.UserData u = new model.UserData();
            u.setId(rs.getInt("user_id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setFullName(  nullSafe(rs, "full_name"));
            u.setPhone(     nullSafe(rs, "phone"));
            u.setProgram(   nullSafe(rs, "program"));
            u.setSemester(  nullSafe(rs, "semester"));
            u.setYearOfStudy(nullSafe(rs, "year_of_study"));
            list.add(u);
        }
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
    } finally {
        mysql.closeConnection(conn);
    }
    return list;
}
 
/** Deletes a student by user_id. Returns true on success. */
public boolean deleteStudent(int userId) {
    String sql = "DELETE FROM users WHERE user_id = ?";
    java.sql.Connection conn = mysql.openConnection();
    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        return ps.executeUpdate() > 0;
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
        return false;
    } finally {
        mysql.closeConnection(conn);
    }
}


    private String nullSafe(ResultSet rs, String col) throws SQLException {
        String val = rs.getString(col);
        return val != null ? val : "";
    }
}