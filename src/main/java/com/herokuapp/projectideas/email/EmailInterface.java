package com.herokuapp.projectideas.email;

import com.herokuapp.projectideas.database.document.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class EmailInterface {

    @Autowired
    EmailService emailService;

    @Autowired
    public SimpleMailMessage unreadMessagesTemplate;

    public void sendUnreadMessagesEmail(User user, int unreadMessages) {
        String text = String.format(
            unreadMessagesTemplate.getText(),
            user.getUsername(),
            unreadMessages,
            "https://projectideas.herokuapp.com/profile"
        );
        emailService.sendEmail(
            user.getEmail(),
            unreadMessagesTemplate.getSubject(),
            text
        );
    }
}
