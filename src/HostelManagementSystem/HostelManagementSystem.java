package HostelManagementSystem;

import database.MYSqlConnector;
import database.db;


public class HostelManagementSystem {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        db database = new MYSqlConnector();
        database.openConnection();
    }
    
}
