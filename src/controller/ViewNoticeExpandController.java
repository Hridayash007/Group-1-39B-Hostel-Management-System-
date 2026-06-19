package controller;

import javax.swing.JDialog;
import javax.swing.JFrame;
import model.NoticeData;
import view.viewnoticeexpand;


public class ViewNoticeExpandController {

    private final viewnoticeexpand view;
    private JDialog dialog;

    public ViewNoticeExpandController(viewnoticeexpand view) {
        this.view = view;
        view.CloseListener(e -> close());
    }

    //Call this to populate the view before opening.

    public void setNotice(NoticeData notice) {
        view.setNoticeData(notice);
    }

    // Opens the expand view as a centered modal dialog over the given parent.
     
    public void open(JFrame parent) {
        dialog = new JDialog(parent, "Notice Details", true);
        dialog.setContentPane(view.getContentPane());
        dialog.setSize(420, 320);   // matches expandnotice panel (400x300) + chrome
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) dialog.dispose();
    }
}