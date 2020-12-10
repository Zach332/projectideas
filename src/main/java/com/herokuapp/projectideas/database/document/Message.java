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
    protected String id;

    protected String senderId;

    @JsonView(View.Get.class)
    protected String senderUsername;

    protected String recipientId;

    @JsonView(View.Base.class)
    protected String content;

    protected long timeSent;

    @JsonView(View.Get.class)
    protected boolean unread;

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
