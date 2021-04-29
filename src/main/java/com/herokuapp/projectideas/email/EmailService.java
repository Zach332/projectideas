package com.herokuapp.projectideas.email;

import com.herokuapp.projectideas.database.document.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    public SimpleMailMessage unreadMessagesTemplate;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("projectideastech@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendUnreadMessagesEmail(User user, int unreadMessages) {
        String text = String.format(
            unreadMessagesTemplate.getText(),
            user.getUsername(),
            unreadMessages,
            "https://projectideas.herokuapp.com/profile"
        );
        sendEmail(user.getEmail(), unreadMessagesTemplate.getSubject(), text);
    }
}
