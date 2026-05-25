package model; 

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    
    private static final String SENDER_EMAIL = "hridayashrestha2007@gmail.com";
    private static final String APP_PASSWORD  = "iarq uszg hwzt hutr"; 

    /**
     * Generates a random 6-digit OTP
     */
    public static String generateOTP() {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }

    /**
     * Sends OTP email to the given recipient
     * @return true if sent successfully, false otherwise
     */
   public static boolean sendOTPEmail(String recipientEmail, String otp) {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "465");
    props.put("mail.smtp.socketFactory.port", "465");
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    props.put("mail.smtp.socketFactory.fallback", "false");
    props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

    Session session = Session.getInstance(props, new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
        }
    });

    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Cityscape Hostel - Password Reset OTP");

        String body = "Dear User,\n\n"
                + "Your OTP for password reset is: " + otp + "\n\n"
                + "This OTP is valid for 5 minutes. Do not share it with anyone.\n\n"
                + "Regards,\nCityscape Hostel Management System";

        message.setText(body);
        Transport.send(message);
        return true;

    } catch (MessagingException e) {
        e.printStackTrace();
        return false;
    }
   }
}