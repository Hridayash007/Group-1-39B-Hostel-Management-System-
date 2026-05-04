package database;
import java.sql.*;


public interface db{
        Connection openConnection();
        void closeConnection(Connection conn);
        ResultSet runQuery(Connection conn, String query);
        int excecuteUpdate(Connection conn, String query);
        }
