package dao;

import database.MYSqlConnector;
import java.sql.*;
import java.util.*;

public class FeeDao {

    private final MYSqlConnector mysql =
            new MYSqlConnector();


    public List<Object[]> getPendingFees(int userId){

        List<Object[]> list = new ArrayList<>();

        String sql =
        "SELECT f.fee_id, " +
        "COALESCE(f.student_name, u.username) AS student_name, " +
        "f.room_number, " +
        "f.amount " +
        "FROM fees f " +
        "INNER JOIN users u ON f.user_id=u.user_id " +
        "WHERE f.user_id=? " +
        "AND f.status='Pending'";


        Connection conn = mysql.openConnection();


        try(PreparedStatement ps =
                conn.prepareStatement(sql)){


            ps.setInt(1,userId);


            ResultSet rs =
                    ps.executeQuery();


            while(rs.next()){


                list.add(new Object[]{

                    rs.getInt("fee_id"),

                    rs.getString("student_name"),

                    rs.getString("room_number"),

                    rs.getDouble("amount")

                });

            }


        }catch(SQLException e){

            e.printStackTrace();

        }
        finally{

            mysql.closeConnection(conn);

        }


        return list;
    }




    /**
     * Inserts a 'Pending' fee row for every allocated student for the current
     * month, but only if they don't already have one for this month.
     * Call this once on application startup — it is safe to call repeatedly
     * because the duplicate-check prevents double-insertion.
     */
    public void generateMonthlyFees() {
        String sql =
            "INSERT INTO fees (user_id, room_id, student_name, room_number, amount, fee_month, status) " +
            "SELECT u.user_id, r.room_id, " +
            "       COALESCE(NULLIF(u.full_name,''), u.username), " +
            "       r.room_number, r.fee, " +
            "       DATE_FORMAT(NOW(), '%M %Y'), " +
            "       'Pending' " +
            "FROM room_allocations ra " +
            "JOIN users u  ON ra.user_id  = u.user_id " +
            "JOIN rooms r  ON ra.room_id  = r.room_id " +
            // Skip students who already have a fee row for this month
            "WHERE NOT EXISTS ( " +
            "    SELECT 1 FROM fees f " +
            "    WHERE f.user_id   = u.user_id " +
            "    AND   f.fee_month = DATE_FORMAT(NOW(), '%M %Y') " +
            ")";

        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            System.out.println("[FeeDao] Monthly fees generated: " + rows + " new row(s) for " + 
                               java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")
                                   .format(java.time.LocalDate.now()));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public List<Object[]> getPaidFees(int userId) {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT p.payment_id, " +
            "DATE_FORMAT(f.paid_date, '%M %d, %Y') AS paid_date, " +
            "f.amount, " +
            "p.status " +
            "FROM payments p " +
            "INNER JOIN fees f ON p.fee_id = f.fee_id " +
            "WHERE p.user_id = ? " +
            "ORDER BY f.paid_date DESC";

        Connection conn = mysql.openConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("payment_id"),
                    rs.getString("paid_date"),
                    "Rs " + rs.getDouble("amount"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public boolean markPaid(int feeId){


        String sql =
        "UPDATE fees SET "
        +"status='Paid', "
        +"paid_date=NOW() "
        +"WHERE fee_id=?";


        Connection con =
                mysql.openConnection();


        try(PreparedStatement ps =
                con.prepareStatement(sql)){


            ps.setInt(1,feeId);


            return ps.executeUpdate()>0;


        }
        catch(SQLException e){

            e.printStackTrace();
            return false;

        }
        finally{

            mysql.closeConnection(con);

        }

    }

public List<Object[]> getAllPayments(String statusFilter) {
    List<Object[]> list = new ArrayList<>();

    String sql =
        "SELECT " +
        "    p.payment_id, " +
        "    COALESCE(f.student_name, u.username) AS student_name, " +
        "    f.room_number, " +
        "    COALESCE(DATE_FORMAT(f.paid_date, '%M %d, %Y'), '—') AS paid_date, " +
        "    f.amount, " +
        "    f.status " +
        "FROM fees f " +
        "INNER JOIN users u ON f.user_id = u.user_id " +
        "LEFT  JOIN payments p ON p.fee_id = f.fee_id " +
        (statusFilter != null ? "WHERE f.status = ? " : "") +
        "ORDER BY f.paid_date DESC, f.fee_id DESC";

    Connection conn = mysql.openConnection();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        if (statusFilter != null) ps.setString(1, statusFilter);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Object[]{
                rs.getInt("payment_id"),        // [0] Payment ID
                rs.getString("student_name"),   // [1] Student
                rs.getString("room_number"),    // [2] Room
                rs.getString("paid_date"),      // [3] Date
                "Rs " + rs.getDouble("amount"), // [4] Amount
                rs.getString("status")          // [5] Status
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        mysql.closeConnection(conn);
    }
    return list;
}
}