package dao;

import database.MYSqlConnector;
import java.sql.*;
import java.util.*;

public class MealDao {

    private final MYSqlConnector mysql = new MYSqlConnector();

    /** Returns all meals keyed by "Monday_Breakfast" etc. Value = items string. */
    public Map<String, String> getAllMeals() {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT day_name, meal_type, items FROM meals ORDER BY FIELD(day_name,"
                   + "'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'),"
                   + "FIELD(meal_type,'Breakfast','Lunch','Dinner')";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                map.put(rs.getString("day_name") + "_" + rs.getString("meal_type"),
                        rs.getString("items"));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.closeConnection(conn); }
        return map;
    }

    /** Insert or update a single meal. */
    public boolean saveMeal(String day, String mealType, String items) {
        String sql = "INSERT INTO meals (day_name, meal_type, items) VALUES (?,?,?) "
                   + "ON DUPLICATE KEY UPDATE items=VALUES(items)";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setString(2, mealType);
            ps.setString(3, items);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { mysql.closeConnection(conn); }
    }

    /** Delete a meal row (so the student panel goes blank). */
    public boolean deleteMeal(String day, String mealType) {
        String sql = "DELETE FROM meals WHERE day_name=? AND meal_type=?";
        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setString(2, mealType);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { mysql.closeConnection(conn); }
    }
}