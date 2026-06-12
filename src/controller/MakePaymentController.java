package controller;


import dao.FeeDao;
import model.UserData;
import view.*;

import javax.swing.JOptionPane;
import java.util.List;
import dao.PaymentDao;
import java.awt.Desktop;
import java.net.URI;
import model.PaymentData;


public class MakePaymentController {
    private final MakePayment view;
    private final FeeDao dao = new FeeDao();
    private final UserData user;
    private final int userId;
    private int feeId;

    public MakePaymentController(
            MakePayment view,
            UserData user) {

        this.view = view;
        this.user = user;
        this.userId = user.getId();

        // welcome text
        view.setWelcomeUser(
                user.getUsername()
        );

        // load pending fees
        loadFees();

        // pay button
        view.PayNowListener(
                e -> payHostelFee(userId, view.getCurrentAmount(), view.getCurrentFeeId())
        );


        // Dashboard
        view.DashboardListener(e -> {
            close();
            StudentDashboard dashboard =new StudentDashboard();
            new StudentDashboardController(dashboard,user).open();
        });

        // Complaints
        view.MyComplaintsListener(e -> {
            close();
            IssueComplaints complaints =new IssueComplaints();
            new IssueComplaintsController(complaints,user).open();
        });

        // Notice
        view.NoticeListener(e -> {
            close();
            ViewNotice notice =new ViewNotice();
            new ViewNoticeController(notice,user).open();
        });

        // My Profile
        view.MyProfileListener(e -> {
            close();
            StudentProfile profile =new StudentProfile();
            new StudentProfileController(profile,user).open();
        });

        // Profile icon
        view.ProfileListener(e -> {
            close();
            StudentProfile profile =new StudentProfile();
            new StudentProfileController(profile,user).open();
        });

        // Sign out
        view.SignOutListener(e -> {
            int confirm =
                    JOptionPane.showConfirmDialog(
                            view,
                            "Are you sure you want to sign out?",
                            "Sign Out",
                            JOptionPane.YES_NO_OPTION
                    );
            if(confirm ==JOptionPane.YES_OPTION){
                close();
                LogIn login =new LogIn();
                new LoginController(login).open();
            }
        });
    }

    private void loadFees(){
        List<Object[]> fees =
                dao.getPendingFees(userId);
        for(Object[] f : fees){
            feeId = Integer.parseInt(f[0].toString());

            String studentName = f[1].toString();
            String room = f[2].toString();
            double amount = Double.parseDouble(f[3].toString());

            view.addPendingFee(studentName, room, amount, feeId);
        }
    }

    private final PaymentDao paymentDao =
            new PaymentDao();

    public void payHostelFee(
            int userId,
            double amount,
            int feeId) {

        if (feeId <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a valid pending fee before paying.");
            return;
        }

        if (amount <= 0) {
            JOptionPane.showMessageDialog(view, "The selected fee amount is invalid.");
            return;
        }

        try {

            var session =
                StripeService.createCheckoutSession(
                        "Hostel Fee",
                        (long)(amount * 100));

            PaymentData payment =
                new PaymentData();

            payment.setUserId(userId);
            payment.setFeeId(feeId);
            payment.setAmount(amount);
            payment.setStripeSessionId(
                    session.getId());

            payment.setStatus("PENDING");

            paymentDao.savePayment(payment);

            if (feeId > 0) {
                dao.markPaid(feeId);
            }

            Desktop.getDesktop()
                    .browse(
                        new URI(
                            session.getUrl()));

            JOptionPane.showMessageDialog(view, "Stripe checkout opened successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Unable to start Stripe payment: " + ex.getMessage());
        }
    }


    public void open(){
        view.setVisible(true);
    }


    private void close(){
        view.dispose();
    }
}