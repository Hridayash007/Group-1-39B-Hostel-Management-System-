package util;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Utility class for profile picture operations.
 * - Opens file chooser for image selection
 * - Copies image to a local app folder (src/resources/profilepics/)
 * - Loads and renders image as a circle inside a JLabel
 */
public class ProfileImageHelper {

    /** Folder where profile pictures are stored inside the project */
    private static final String PROFILE_PICS_DIR = "src/resources/profilepics/";

    /**
     * Opens a file chooser, copies the selected image to the app folder,
     * and returns the stored relative path. Returns null if cancelled.
     */
    public static String chooseAndSaveImage(int userId) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Picture");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files (jpg, png, jpeg)", "jpg", "jpeg", "png"));

        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return null;

        File selectedFile = chooser.getSelectedFile();
        String ext = getExtension(selectedFile.getName());
        String savedFileName = "user_" + userId + "." + ext;

        // Create the profile pics directory if it doesn't exist
        File dir = new File(PROFILE_PICS_DIR);
        if (!dir.exists()) dir.mkdirs();

        File dest = new File(PROFILE_PICS_DIR + savedFileName);
        try {
            Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return PROFILE_PICS_DIR + savedFileName; // return stored path
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to save profile picture.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Loads an image from the given path and renders it as a circle
     * inside a JLabel of the given size.
     * Falls back to a default avatar icon if the path is null/invalid.
     */
    public static JLabel createCircularImageLabel(String imagePath, int size) {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Clip to circle
                g2.setClip(new Ellipse2D.Float(0, 0, size, size));

                if (getIcon() instanceof ImageIcon icon) {
                    g2.drawImage(icon.getImage(), 0, 0, size, size, null);
                } else {
                    // Draw default grey avatar
                    g2.setColor(new Color(209, 213, 219));
                    g2.fillOval(0, 0, size, size);
                    g2.setColor(new Color(107, 114, 128));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
                    FontMetrics fm = g2.getFontMetrics();
                    String text = "?";
                    int tx = (size - fm.stringWidth(text)) / 2;
                    int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(text, tx, ty);
                }
                g2.dispose();
            }
        };

        label.setPreferredSize(new Dimension(size, size));
        label.setSize(size, size);
        label.setOpaque(false);

        // Load image
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    // Resize to square
                    BufferedImage square = resizeToSquare(img, size);
                    label.setIcon(new ImageIcon(square));
                } catch (IOException e) {
                    // Fall through to default avatar
                }
            }
        }

        return label;
    }

    /**
     * Sets an existing JLabel to show the profile image as a circle.
     * Use this to update a label already placed in the form.
     */
    public static void applyCircularImage(JLabel label, String imagePath, int size) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    BufferedImage square = resizeToSquare(img, size);
                    label.setIcon(new ImageIcon(square));
                    return;
                } catch (IOException ignored) {}
            }
        }
        // Default: show no icon (label renders grey circle via paintComponent)
        label.setIcon(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static BufferedImage resizeToSquare(BufferedImage original, int size) {
        // Crop to square first (center crop)
        int origW = original.getWidth();
        int origH = original.getHeight();
        int cropSize = Math.min(origW, origH);
        int x = (origW - cropSize) / 2;
        int y = (origH - cropSize) / 2;
        BufferedImage cropped = original.getSubimage(x, y, cropSize, cropSize);

        // Scale to target size
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(cropped, 0, 0, size, size, null);
        g2.dispose();
        return scaled;
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "png";
    }
}