package com.herokuapp.projectideas.database.document.message;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.View;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ReceivedMessage {

    @JsonView(View.Get.class)
    protected String id;

    protected String type;

    /**
     * Id of the user that received the message
     */
    protected String userId;

    @JsonView(View.Get.class)
    protected String senderUsername;

    @JsonView(View.Base.class)
    protected String content;

    protected long timeSent;

    @JsonView(View.Get.class)
    protected boolean unread;

    public ReceivedMessage(
        String recipientId,
        String senderUsername,
        String content
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "ReceivedMessage";
        this.userId = recipientId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.timeSent = Instant.now().getEpochSecond();
        this.unread = true;
    }
}
