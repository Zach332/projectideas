package com.herokuapp.projectideas.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

@Configuration
public class EmailTemplates {

    @Bean
    public SimpleMailMessage unreadMessagesTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(unreadMessagesText);
        message.setSubject("Unread Messages");
        return message;
    }

    private static String unreadMessagesText =
        """
        Hi %s,

        You have %d unread messages on projectideas. See your unread messages at https://projectideas.herokuapp.com/messages.

        Messages usually indicate that another user sent you a message, or someone requested to join your project team.

        Thanks,
        projectideas

        --

        If you would like to unsubscribe, click here: %s

        """;
}
