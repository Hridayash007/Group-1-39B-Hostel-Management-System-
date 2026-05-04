package database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.*;

public class MYSqlConnector implements db{
    @Override
    public Connection openConnection(){
        try{
            String username="root";
            String password="1234";
            String database="hostelmanagementsystem";
            Connection connection;
            connection = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/"+database,username,password
            );
            if (connection==null){
                System.out.println("Connection Null");
            }else{
                System.out.println("Connection Success");
            }
                return connection;
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }

     

    @Override
    public void closeConnection(Connection conn) {
        try{
            if(conn != null && !conn.isClosed() ){
                conn.close();
                System.out.println("Connection close");
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }


    @Override
   public ResultSet runQuery(Connection conn, String query) {
       try{
           Statement stmp = conn.createStatement();
           ResultSet result = stmp.executeQuery(query);
           return result;
       }catch (Exception e){
           System.out.println(e);
           return null;
       }
    }


    @Override
   public int excecuteUpdate(Connection conn, String query) {
      try{
          Statement stmp = conn.createStatement();
          int result = stmp.executeUpdate(query);
          return result;
      }catch(Exception e){
          System.out.println(e);
          return -1;
      }
    }
}