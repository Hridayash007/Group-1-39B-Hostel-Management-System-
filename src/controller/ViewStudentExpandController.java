package controller;

import javax.swing.JDialog;
import javax.swing.JFrame;
import model.UserData;
import view.ViewStudentExpand;

/**
 * Opens ViewStudentExpand as a modal dialog centered over the parent window.
 * MVC-compliant: view only exposes setStudentData() and CloseListener().
 */
public class ViewStudentExpandController {

    private final ViewStudentExpand view;
    private JDialog dialog;

    public ViewStudentExpandController(ViewStudentExpand view) {
        this.view = view;
        view.CloseListener(e -> close());
    }

    public void setStudent(UserData user) {
        view.setStudentData(user);
    }

    /**
     * Opens as a centered modal dialog over the given parent window.
     */
    public void open(JFrame parent) {
        dialog = new JDialog(parent, "Student Details", true);
        dialog.setContentPane(view.getContentPane());
        dialog.setSize(370, 320);   // matches viewstudent panel (350x300) + chrome
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) dialog.dispose();
    }
}