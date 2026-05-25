package dao;

import database.MYSqlConnector;
import model.UserData;
import java.sql.*;

public class UserDao {

    MYSqlConnector mysql = new MYSqlConnector();

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

    // validates login credentials against the database
    public UserData loginUser(String email, String password) {
        Connection conn = mysql.openConnection();
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, email);
            pstm.setString(2, password);
            ResultSet result = pstm.executeQuery();
            if (result.next()) {
                UserData user = new UserData();
                user.setId(result.getInt("user_id"));
                user.setUsername(result.getString("username"));
                user.setEmail(result.getString("email"));
                return user; // credentials matched
            }
        } catch (SQLException ex) {
            System.out.print(ex);
        } finally {
            mysql.closeConnection(conn);
        }
        return null; // no match found
    }


// Check if email exists
public boolean checkEmailExists(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        Connection con = mysql.openConnection();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(con);
        }
    }

    
    public boolean updatePassword(String email, String newPassword, String newconfirmPassword) {
        String query = "UPDATE users SET password = ? ,confirm_password=? WHERE email = ?";
        Connection con = mysql.openConnection();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, newPassword);
            ps.setString(2, newconfirmPassword);
            ps.setString(3, email);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            mysql.closeConnection(con);
        }
    }
}