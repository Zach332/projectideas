package com.herokuapp.projectideas.database.document.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ReceivedGroupMessage extends ReceivedMessage {

    protected String recipientProjectId;
    protected String recipientProjectName;

    public ReceivedGroupMessage(
        String recipientId,
        String senderUsername,
        String content,
        String recipientProjectId,
        String recipientProjectName
    ) {
        super(recipientId, senderUsername, content);
        this.type = "ReceivedGroupMessage";
        this.recipientProjectId = recipientProjectId;
        this.recipientProjectName = recipientProjectName;
    }
}
