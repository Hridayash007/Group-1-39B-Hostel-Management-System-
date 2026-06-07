package controller;

import dao.ComplaintDao;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import model.ComplaintData;
import model.UserData;
import view.issueComplaintExpand;

/**
 * Opens issueComplaintExpand as a centered modal dialog.
 * Handles complaint submission to DB.
 */
public class IssueComplaintExpandController {

    private final ComplaintDao dao = new ComplaintDao();
    private final issueComplaintExpand view;
    private final UserData user;
    private final IssueComplaintsController parent;
    private JDialog dialog;

    public IssueComplaintExpandController(issueComplaintExpand view,
                                          UserData user,
                                          IssueComplaintsController parent) {
        this.view   = view;
        this.user   = user;
        this.parent = parent;

        view.SubmitListener(e -> submit());
        view.CancelListener(e -> close());
    }

    private void submit() {
        String title    = view.getTitleField().getText().trim();
        String desc     = view.getDescriptionArea().getText().trim();
        String category = view.getCategoryCombo().getSelectedItem().toString();
        String priority = view.getPriorityCombo().getSelectedItem().toString();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a complaint title.");
            return;
        }
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a description.");
            return;
        }

        ComplaintData c = new ComplaintData();
        c.setUserId(user.getId());
        c.setTitle(title);
        c.setDescription(desc);
        c.setCategory(category);
        c.setPriority(priority);
        c.setStatus("Pending");

        boolean saved = dao.createComplaint(c);
        if (saved) {
            JOptionPane.showMessageDialog(dialog,
                    "Complaint submitted successfully!", "Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
            close();
            parent.refresh();
        } else {
            JOptionPane.showMessageDialog(dialog,
                    "Failed to submit complaint. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void open(JFrame parentFrame) {
        dialog = new JDialog(parentFrame, "File New Complaint", true);
        dialog.setContentPane(view.getContentPane());
        dialog.setSize(375, 320);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) dialog.dispose();
    }
}