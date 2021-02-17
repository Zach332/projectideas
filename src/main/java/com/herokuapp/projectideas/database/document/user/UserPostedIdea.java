package com.herokuapp.projectideas.database.document.user;

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
    protected long timePosted;

    public UserPostedIdea(String userId, String ideaId) {
        this.id = UUID.randomUUID().toString();
        this.type = "UserPostedIdea";
        this.userId = userId;
        this.ideaId = ideaId;
        this.timePosted = Instant.now().getEpochSecond();
    }

    public String getPartitionKey() {
        return userId;
    }
}
