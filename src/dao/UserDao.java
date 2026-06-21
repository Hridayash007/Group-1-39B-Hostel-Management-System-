package dao;

import database.MYSqlConnector;
import model.UserData;
import util.PasswordUtil;
import java.sql.*;

public class UserDao {

    MYSqlConnector mysql = new MYSqlConnector();

    // ── Registration ──────────────────────────────────────────────────────────
    public void createUser(UserData user) {
        Connection conn = mysql.openConnection();
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, user.getUsername());
            pstm.setString(2, user.getEmail());
            // Every NEW signup gets a hashed password. Existing plaintext
            // rows in the database are left untouched per your instruction —
            // loginUser() below handles both cases at login time.
            pstm.setString(3, PasswordUtil.hash(user.getPassword()));
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

    // ── Login ─────────────────────────────────────────────────────────────────
    public UserData loginUser(String username, String password) {
        
        Connection conn = mysql.openConnection();
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, username);
            pstm.setString(2, username);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                boolean passwordMatches;
                if (PasswordUtil.isHashed(storedPassword)) {
                    // New-style account — verify against the BCrypt hash.
                    passwordMatches = PasswordUtil.verify(password, storedPassword);
                } else {
                    // Legacy plaintext account (left as-is per your instruction)
                    // — fall back to a direct comparison.
                    passwordMatches = storedPassword.equals(password);
                }

                if (passwordMatches) return mapFullRow(rs);
            }
        } catch (SQLException ex) {
            System.out.print(ex);
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    // ── Fetch by user_id ──────────────────────────────────────────────────────
    public UserData getStudentDetails(int userId) {
        Connection conn = mysql.openConnection();
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapFullRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    // ── Update student details (includes profile image path) ─────────────────
    public boolean updateStudentDetails(UserData user) {
        String sql = "UPDATE users SET "
                + "full_name=?, phone=?, date_of_birth=?, nationality=?, "
                + "program=?, year_of_study=?, semester=?, address=?, "
                + "ec_name=?, ec_relation=?, ec_number=?, email=?, userimage=? "
                + "WHERE user_id=?";
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
            ps.setString(13, user.getUserImage());  // String path
            ps.setInt(14,    user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ── Update only the profile image ─────────────────────────────────────────
    public boolean updateProfileImage(int userId, String imagePath) {
        String sql = "UPDATE users SET userimage = ? WHERE user_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imagePath);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ── Password reset ────────────────────────────────────────────────────────
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

    public boolean updatePassword(String email, String newPassword) {
        Connection conn = mysql.openConnection();
        String query = "UPDATE users SET password=? WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ── All students ──────────────────────────────────────────────────────────
    public java.util.List<UserData> getAllStudents() {
        java.util.List<UserData> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFullRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public boolean deleteStudent(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private UserData mapFullRow(ResultSet rs) throws SQLException {
        UserData user = new UserData();
        user.setId(         rs.getInt("user_id"));
        user.setUsername(   rs.getString("username"));
        user.setEmail(      rs.getString("email"));
        user.setPassword(   rs.getString("password"));
        user.setFullName(   nullSafe(rs, "full_name"));
        user.setPhone(      nullSafe(rs, "phone"));
        user.setDateOfBirth(nullSafe(rs, "date_of_birth"));
        user.setNationality(nullSafe(rs, "nationality"));
        user.setProgram(    nullSafe(rs, "program"));
        user.setYearOfStudy(nullSafe(rs, "year_of_study"));
        user.setSemester(   nullSafe(rs, "semester"));
        user.setAddress(    nullSafe(rs, "address"));
        user.setEcName(     nullSafe(rs, "ec_name"));
        user.setEcRelation( nullSafe(rs, "ec_relation"));
        user.setEcNumber(   nullSafe(rs, "ec_number"));
        user.setUserImage(  nullSafe(rs, "userimage"));  // load image path
        return user;
    }

    private String nullSafe(ResultSet rs, String col) throws SQLException {
        String val = rs.getString(col);
        return val != null ? val : "";
    }
}