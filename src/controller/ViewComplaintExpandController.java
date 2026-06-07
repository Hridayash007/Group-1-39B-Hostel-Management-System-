package controller;

import javax.swing.JDialog;
import javax.swing.JFrame;
import model.ComplaintData;
import view.ViewComplaintExpand;

/**
 * Opens ViewComplaintExpand as a modal dialog centered over the parent window.
 * Used by both IssueComplaintsController (student) and ViewComplaintController (admin).
 */
public class ViewComplaintExpandController {

    private final ViewComplaintExpand view;
    private JDialog dialog;

    public ViewComplaintExpandController(ViewComplaintExpand view) {
        this.view = view;
        view.CloseListener(e -> close());
    }

    /** Populate the expand view with complaint data before opening. */
    public void setComplaint(ComplaintData c) {
        view.setComplaintData(c);
    }

    /**
     * Opens as a centered modal dialog over the given parent window.
     * @param parent  the JFrame to center over (IssueComplaints or ViewComplaint)
     */
    public void open(JFrame parent) {
        dialog = new JDialog(parent, "Complaint Details", true);
        dialog.setContentPane(view.getContentPane());
        dialog.setSize(380, 360);          // matches viewcomplaint panel (350x330) + chrome
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) dialog.dispose();
    }
}