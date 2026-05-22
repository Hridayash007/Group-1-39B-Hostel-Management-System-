package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewClass {

    private database.MYSqlConnector connector = new database.MYSqlConnector();

    public boolean checkEmailExists(String email) {
        String query = "SELECT * FROM students WHERE email = ?";
        Connection con = connector.openConnection();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            connector.closeConnection(con);
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        String query = "UPDATE students SET password = ? WHERE email = ?";
        Connection con = connector.openConnection();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            connector.closeConnection(con);
        }
    }
}