package com.herokuapp.projectideas.email;

import com.herokuapp.projectideas.database.document.user.User;
import freemarker.template.Template;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Component
public class EmailInterface {

    @Autowired
    EmailService emailService;

    @Autowired
    private Template unreadMessagesTemplate;

    public void sendUnreadMessagesEmail(User user, int unreadMessages) {
        Map<String, String> templateModel = new HashMap<String, String>();
        templateModel.put("username", user.getUsername());
        templateModel.put("numUnread", String.valueOf(unreadMessages));
        try {
            String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(
                unreadMessagesTemplate,
                templateModel
            );
            emailService.sendHtmlEmail(
                user.getEmail(),
                "New Unread Messages",
                htmlBody
            );
        } catch (Exception ignored) {}
    }
}
