package com.herokuapp.projectideas.database.document.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.herokuapp.projectideas.database.document.RootDocument;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class UserPostedIdea implements RootDocument {

    protected String id;
    protected String type;
    protected String userId;
    protected String ideaId;
    protected long timeCreated;

    @JsonProperty("_etag")
    protected String etag;

    public UserPostedIdea(String userId, String ideaId) {
        this.id = UUID.randomUUID().toString();
        this.type = "UserPostedIdea";
        this.userId = userId;
        this.ideaId = ideaId;
        this.timeCreated = Instant.now().getEpochSecond();
    }

    public String getPartitionKey() {
        return userId;
    }
}
