package com.herokuapp.projectideas.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class MessageNotification extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String recipientId;

    public MessageNotification(Object source, String recipientId) {
        super(source);
        this.recipientId = recipientId;
    }
}
