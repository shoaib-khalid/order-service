/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author user
 */
@org.springframework.stereotype.Service
public class EmailService {
    
    private static Logger logger = LoggerFactory.getLogger("application");
    
    //@Autowired
    @Value("${emailService.SenderAddress:mcmc.lms@gmail.com}")
    static String emailServiceSenderAddress;
    
    public static void SendEmail(String emailAddress, String emailSubject, String emailText) {
          logger.info("SendEmail() starting");
            
          logger.info("mail:"+emailAddress+" emailSubject:"+emailSubject);
          
          String from = "no-reply@symplified.biz";
          
            // Recipient's email ID needs to be mentioned.
          String to = emailAddress;
          
          // Assuming you are sending email from localhost
          String host = "localhost";

          // Setup mail server
          Properties prop = new Properties();
          prop.put("mail.smtp.auth", true);
          prop.put("mail.smtp.starttls.enable", "true");
          prop.put("mail.smtp.host", "smtpout.secureserver.net");
          prop.put("mail.smtp.port", "465");
          // SSL Factory
          prop.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");  

          //prop.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");

          // creating Session instance referenced to 
            // Authenticator object to pass in 
            // Session.getInstance argument
            Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {

                    // override the getPasswordAuthentication 
                    // method
                    protected PasswordAuthentication 
                            getPasswordAuthentication() {
                        return new PasswordAuthentication("no-reply@symplified.biz",
                                                        "SYMplified@1234");
                    }
                });

          try {
             // Create a default MimeMessage object.
             MimeMessage message = new MimeMessage(session);

             // Set From: header field of the header.
             message.setFrom(new InternetAddress(from));

             // Set To: header field of the header.
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

             // Set Subject: header field
             message.setSubject(emailSubject);

             // Send the actual HTML message, as big as you like
             message.setContent(emailText, "text/html");

             // Send message
             Transport.send(message);
             logger.info("Sent message successfully....");
          } catch (MessagingException mex) {
             mex.printStackTrace();
             logger.error("Exception sending email:",mex);
          }
    }
}
