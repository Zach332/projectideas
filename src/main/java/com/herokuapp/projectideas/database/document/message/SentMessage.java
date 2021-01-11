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
    { @Type(SentIndividualMessage.class), @Type(SentGroupMessage.class) }
)
public abstract class SentMessage extends Message {

    protected SentMessage(String senderId, String content) {
        super(senderId, content);
        this.type = "SentMessage";
    }
}
