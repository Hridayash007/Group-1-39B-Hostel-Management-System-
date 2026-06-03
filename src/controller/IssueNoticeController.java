package controller;

import dao.NoticeDao;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import model.NoticeData;
import view.IssueNotice;

public class IssueNoticeController {

    private final NoticeDao noticeDao = new NoticeDao();
    private final IssueNotice view;
    private final NoticeAdminController parentController;
    private JDialog dialog;

    public IssueNoticeController(IssueNotice view, NoticeAdminController parent) {
        this.view             = view;
        this.parentController = parent;

        view.PublishListener(e -> publishNotice());
        view.CancelListener(e ->  close());
    }

    private void publishNotice() {
        String title    = view.getTitleField().getText().trim();
        String desc     = view.getDescriptionArea().getText().trim();
        String category = view.getCategoryCombo().getSelectedItem().toString();
        String priority = view.getPriorityCombo().getSelectedItem().toString();
        boolean pinned  = view.isPinned();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a notice title.");
            return;
        }
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a description.");
            return;
        }

        NoticeData notice = new NoticeData(title, desc, category, priority, pinned);
        boolean saved = noticeDao.createNotice(notice);

        if (saved) {
            JOptionPane.showMessageDialog(dialog,
                    "Notice published successfully!", "Published",
                    JOptionPane.INFORMATION_MESSAGE);
            close();
            parentController.refresh();
        } else {
            JOptionPane.showMessageDialog(dialog,
                    "Failed to publish notice. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void open() {
        dialog = new JDialog(parentController.getView(), "New Notice", true);

        // IssueNotice uses null layout with fixed bounds — must set size explicitly
        dialog.setContentPane(view.getContentPane());
        dialog.setSize(380, 340);          // matches issuenoticepanel bounds (350x300) + padding
        dialog.setLocationRelativeTo(parentController.getView());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) dialog.dispose();
    }
}