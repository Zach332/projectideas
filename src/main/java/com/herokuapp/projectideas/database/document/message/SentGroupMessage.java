package com.herokuapp.projectideas.database.document.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class SentGroupMessage extends SentMessage {

    protected String recipientProjectId;
    protected String recipientProjectName;

    public SentGroupMessage(
        String senderId,
        String recipientProjectId,
        String recipientProjectName,
        String content
    ) {
        super(senderId, content);
        this.type = "SentGroupMessage";
        this.recipientProjectId = recipientProjectId;
    }
}
