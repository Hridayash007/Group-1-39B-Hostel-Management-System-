package HostelManagementSystem;

import controller.LoginController;
import view.LogIn;
import database.db;
import database.MYSqlConnector;

public class HostelManagementSystem {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            // FIXED: Start from Login screen, not Signup
            LogIn view = new LogIn();
            LoginController controller = new LoginController(view);
            controller.open();
        });
    }
}

