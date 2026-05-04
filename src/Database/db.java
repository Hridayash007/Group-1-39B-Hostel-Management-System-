package atabase;
import java.sql.Connection;

public interface db{
        Connection openConnection();
        void closeConnection(Connection conn);
        }
