package dao;

import database.MYSqlConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import model.PaymentData;

public class PaymentDao {
private final MYSqlConnector mysql = new MYSqlConnector();

    public boolean savePayment(PaymentData p) {

        String sql =
            "INSERT INTO payments(user_id, fee_id, amount, stripe_session_id, status) "
            + "VALUES(?,?,?,?,?)";
        Connection con = mysql.openConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getUserId());
            ps.setInt(2, p.getFeeId());
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getStripeSessionId());
            ps.setString(5, p.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}