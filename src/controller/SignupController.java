/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;


import dao.UserDao;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import model.UserData;
import view.Login;
import view.signup;


/**
 *
 * @author User
 */
public class SignupController {
    private final UserDao userDao = new UserDao();
    private final signup userView;

    public SignupController(signup userView) {
        this.userView = userView;

        userView.addAddUserListener(new AddUserListener());
        userView.addLoginListener(new LoginListener());
        

    }

    public void open() {
        this.userView.setVisible(true);
    }

    public void close() {
        this.userView.dispose();
    }

    class AddUserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String name = userView.getUsernameField().getText();
                String email = userView.getEmailField().getText();
                String password = userView.getPasswordField().getText();
                UserData user = new UserData(name, email, password);
               
                boolean check = userDao.checkUser(user);

                if (check) {
                    JOptionPane.showMessageDialog(userView, "Duplicate user");
                } else {
                    userDao.createUser(user);
                    JOptionPane.showMessageDialog(userView, "Succesful");

                }
            } catch (Exception ex) {
                System.out.println("Error adding user: " + ex.getMessage());
            }

        }

    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Login loginView = new Login();
            LoginController login = new LoginController(loginView);
            close();
            login.open();
        }
    }
    

}
