package controller;

import view.ReportAfterPayment;
import model.UserData;
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.File;

// Import PDF Generation Library classes
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class ReportAfterPaymentController {
    private final ReportAfterPayment view;
    private final UserData user;
    private final int feeId;
    private final double amount;
    private JDialog dialog;

    public ReportAfterPaymentController(ReportAfterPayment view, UserData user, int feeId, double amount) {
        this.view = view;
        this.user = user;
        this.feeId = feeId;
        this.amount = amount;

        // Populate details on the UI frame using your user data carrier object
        this.view.setReceiptDetails(user.getUsername(), String.valueOf(user.getId()), amount, "Completed");

        // Set up action listeners
        this.view.getDownloadButton().addActionListener(e -> generatePDFReceipt());
        this.view.getCancelButton().addActionListener(e -> close());
    }

    private void generatePDFReceipt() {
        // 1. Configure JFileChooser to prompt the student where to save their receipt
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Payment Receipt PDF");
        fileChooser.setSelectedFile(new File("Hostel_Receipt_Fee_" + feeId + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(dialog);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; // Exit if the student cancels the save dialog
        }

        File fileToSave = fileChooser.getSelectedFile();
        
        // 2. Build the PDF Document structure
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();

            // Font configurations
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            // Receipt Header Design
            Paragraph title = new Paragraph("CITYSCAPE HOSTEL MANAGEMENT SYSTEM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Official Payment Receipt\n\n", headerFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph("========================================================\n\n", normalFont));

            // Create a structured data grid table for layout accuracy
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Append student metadata information rows
            table.addCell(new Paragraph(" Student Name:", headerFont));
            table.addCell(new Paragraph(" " + user.getUsername(), normalFont));

            table.addCell(new Paragraph(" Student ID:", headerFont));
            table.addCell(new Paragraph(" " + user.getId(), normalFont));

            table.addCell(new Paragraph(" Fee Record Reference ID:", headerFont));
            table.addCell(new Paragraph(" " + feeId, normalFont));

            table.addCell(new Paragraph(" Clearances Amount:", headerFont));
            table.addCell(new Paragraph(" Rs " + amount, normalFont));

            table.addCell(new Paragraph(" Transaction Status:", headerFont));
            table.addCell(new Paragraph(" COMPLETED (Verified via Stripe Gateway)", normalFont));

            document.add(table);

            document.add(new Paragraph("\n\n========================================================", normalFont));
            Paragraph footer = new Paragraph("Thank you for your clearance! This is a system-generated secure slip.", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            JOptionPane.showMessageDialog(dialog, "Receipt downloaded successfully at:\n" + fileToSave.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error generating receipt PDF: " + ex.getMessage());
        } finally {
            if (document.isOpen()) {
                document.close(); // Clean resource release
            }
        }
    }

    public void open(JFrame parentFrame) {
        dialog = new JDialog(parentFrame, "Payment Receipt", true);
        dialog.setContentPane(view.getContentPane());
        dialog.setSize(383, 320);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) dialog.dispose();
    }
}