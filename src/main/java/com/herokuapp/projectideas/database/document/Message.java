package com.herokuapp.projectideas.database.document;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.View;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Message {

    @JsonView(View.Get.class)
    String id;

    String senderId;

    @JsonView(View.Get.class)
    String senderUsername;

    String recipientId;

    @JsonView(View.Base.class)
    String content;

    long timeSent;

    @JsonView(View.Get.class)
    boolean unread;

    public Message(
        String senderId,
        String senderUsername,
        String recipientId,
        String content
    ) {
        this.id = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.recipientId = recipientId;
        this.content = content;
        this.timeSent = Instant.now().getEpochSecond();
        this.unread = true;
    }
}
