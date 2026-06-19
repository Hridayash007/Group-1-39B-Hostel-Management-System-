package controller;

import dao.FeeDao;
import dao.PaymentDao;
import model.PaymentData;
import model.UserData;
import view.*;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class MakePaymentController {
    private final MakePayment view;
    private final FeeDao dao = new FeeDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final UserData user;
    private final int userId;

    public MakePaymentController(MakePayment view, UserData user) {
        this.view = view;
        this.user = user;
        this.userId = user.getId();

 String stripeKey = System.getenv("STRIPE_SECRET_KEY");
        if (stripeKey == null || stripeKey.isBlank()) {
            JOptionPane.showMessageDialog(view,
                    "Payment system is not configured (missing STRIPE_SECRET_KEY).\n" +
                    "Please contact the system administrator.",
                    "Configuration Error", JOptionPane.ERROR_MESSAGE);
        }
        Stripe.apiKey = stripeKey;
        view.setWelcomeUser(user.getUsername());
        loadFees();
        loadPaymentHistory();

        view.PayNowListener(e -> pay());

        view.DashboardListener(e -> { close(); new StudentDashboardController(new StudentDashboard(), user).open(); });
        view.MyProfileListener(e -> { close(); new StudentProfileController(new StudentProfile(), user).open(); });
        view.ProfileListener(e -> { close(); new StudentProfileController(new StudentProfile(), user).open(); });
        view.MyComplaintsListener(e -> { close(); new IssueComplaintsController(new IssueComplaints(), user).open(); });
        view.NoticeListener(e -> { close(); new ViewNoticeController(new ViewNotice(), user).open(); });
        view.RoomDetailsListener(e -> { close(); new RoomDetailsStudentController(new RoomDetailsStudent(), user).open(); });
        view.MealRoutineListener(e -> { close(); new StudentMealRoutineController(new StudentMealRoutine(), user).open(); });
        view.PaymentHistoryListener(e -> { close(); new ViewPaymentDetailsController(new ViewPaymentDetails(), user).open(); });
        //top right notice
        view.NotificatinListener(e -> {
            close();
            new ViewNoticeController(new ViewNotice(), user).open();
        });
        view.SignOutListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to sign out?", "Sign Out",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                close();
                new LoginController(new LogIn()).open();
            }
        });
    }

    private void pay() {
        int feeId = view.getCurrentFeeId();
        double amount = view.getCurrentAmount();

        if (feeId == 0 || amount <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a valid fee to pay.");
            return;
        }

        view.setPayNowEnabled(false);

        try {
            long amountInCents = (long) (amount * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://checkout.stripe.dev/success")
                .setCancelUrl("https://checkout.stripe.dev/cancel")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("npr") 
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Hostel Fee Payment (ID: " + feeId + ")")
                                        .setDescription("Room charge for Student ID: " + userId)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

            Session session = Session.create(params);
            String checkoutUrl = session.getUrl();
            String stripeSessionId = session.getId();

            // Open browser for payment
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(checkoutUrl));
            } else {
                throw new Exception("Desktop browsing is not supported.");
            }

            // ── Ask the student to confirm after completing payment ────────────
            // Then poll Stripe to verify — do NOT mark paid based on browser open alone
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Click YES once you've completed payment in the browser tab.\n\n" +
                    "We will then verify the payment with Stripe directly.\n" +
                    "(If you already paid but click NO, your fee will still show as " +
                    "pending here — use 'Payment History' or contact admin to resolve.)",
                    "Confirm Payment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                // Student said No or closed dialog — do NOT mark paid
                view.setPayNowEnabled(true);
                return;
            }

            // ── Poll Stripe to verify payment status (runs off EDT) ──────────
            view.setPayNowEnabled(false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                String verifiedStatus = "unknown";

                @Override
                protected Boolean doInBackground() throws Exception {
                    // Poll Stripe up to 10 times (20 seconds) waiting for "paid"
                    for (int i = 0; i < 10; i++) {
                        Session latest = Session.retrieve(stripeSessionId);
                        verifiedStatus = latest.getPaymentStatus(); // "paid", "unpaid", "no_payment_required"

                        if ("paid".equals(verifiedStatus)) return true;

                        Thread.sleep(2000); // wait 2 seconds between polls
                    }
                    return false;
                }

                @Override
                protected void done() {
                    try {
                        boolean paid = get();

                        if (paid) {
                            // ── Payment confirmed by Stripe ───────────────────
                            boolean marked = dao.markPaid(feeId);
                            if (marked) {
                                PaymentData p = new PaymentData();
                                p.setUserId(userId);
                                p.setFeeId(feeId);
                                p.setAmount(amount);
                                p.setStripeSessionId(stripeSessionId);
                                p.setStatus("Paid"); 
                                paymentDao.savePayment(p);

                                JOptionPane.showMessageDialog(view,
                                        "✅ Payment verified and recorded successfully!",
                                        "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

                                new ReportAfterPaymentController(
                                        new ReportAfterPayment(), user, feeId, amount).open(view);

                                view.clearPendingPanel();
                                loadFees();
                                loadPaymentHistory();
                            }
                        } else {
                            // ── Stripe did NOT confirm payment ────────────────
                            JOptionPane.showMessageDialog(view,
                                    "⚠️ Payment not confirmed by Stripe.\n\n" +
                                    "Status: " + verifiedStatus + "\n\n" +
                                    "The fee remains pending. Please try again.",
                                    "Payment Not Confirmed", JOptionPane.WARNING_MESSAGE);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(view,
                                "Error verifying payment: " + ex.getMessage(),
                                "Verification Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        view.setPayNowEnabled(true);
                    }
                }
            };

            worker.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view,
                    "Error initiating payment: " + ex.getMessage(),
                    "Payment Error", JOptionPane.ERROR_MESSAGE);
            view.setPayNowEnabled(true);
        }
    }

    private void loadFees() {
        view.clearPendingPanel();
        List<Object[]> fees = dao.getPendingFees(userId);
        if (fees.isEmpty()) return;
        for (Object[] f : fees) {
            int id = Integer.parseInt(f[0].toString());
            String studentName = String.valueOf(f[1]);
            String room = String.valueOf(f[2]);
            double amt = f[3] != null ? Double.parseDouble(f[3].toString()) : 0;
            view.addPendingFee(studentName, room, amt, id);
        }
    }

    private void loadPaymentHistory() {
        List<Object[]> history = dao.getPaidFees(userId);
        String[] cols = {"Payment ID", "Payment Date", "Amount", "Status"};
        javax.swing.table.DefaultTableModel model =
                new javax.swing.table.DefaultTableModel(cols, 0) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                };
        for (Object[] row : history) model.addRow(row);
        view.getPaymentHistoryTable().setModel(model);
        view.getPaymentHistoryTable().setRowHeight(28);
    }

    public void open()  { view.setVisible(true); }
    public void close() { view.dispose(); }
}