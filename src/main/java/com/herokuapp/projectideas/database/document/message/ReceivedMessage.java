package com.herokuapp.projectideas.database.document.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    {
        @Type(ReceivedIndividualMessage.class),
        @Type(ReceivedGroupMessage.class),
    }
)
public abstract class ReceivedMessage extends Message {

    protected String senderUsername;
    protected boolean unread;

    protected ReceivedMessage(
        String recipientId,
        String senderUsername,
        String content
    ) {
        super(recipientId, content);
        this.type = "ReceivedMessage";
        this.senderUsername = senderUsername;
        this.unread = true;
    }
}
