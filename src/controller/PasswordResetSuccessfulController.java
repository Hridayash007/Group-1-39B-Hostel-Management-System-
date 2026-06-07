package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import view.LogIn;
import view.PasswordResetSuccessful;

public class PasswordResetSuccessfulController {

    private final PasswordResetSuccessful view;

    public PasswordResetSuccessfulController(PasswordResetSuccessful view) {
        this.view = view;
        view.GoToLoginListener(new GoToLoginListener());
    }

    public void open() {
        view.setVisible(true);
    }

    public void close() {
        view.dispose();
    }

    class GoToLoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
            LogIn loginView = new LogIn();
            new LoginController(loginView).open();
        }
    }
}