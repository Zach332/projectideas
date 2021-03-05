package com.herokuapp.projectideas.database.document.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.herokuapp.projectideas.database.document.RootDocument;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({ @Type(ReceivedMessage.class), @Type(SentMessage.class) })
public abstract class Message implements RootDocument {

    protected String id;
    protected String type;
    /**
     * Id of the user that sent the message
     */
    protected String userId;
    protected String content;
    protected long timeSent;

    @JsonProperty("_etag")
    protected String etag;

    protected Message(String userId, String content) {
        this.id = UUID.randomUUID().toString();
        this.type = "Message";
        this.userId = userId;
        this.content = content;
        this.timeSent = Instant.now().getEpochSecond();
    }

    public String getPartitionKey() {
        return userId;
    }
}
