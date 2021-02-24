package com.herokuapp.projectideas.database.document.post;

import com.herokuapp.projectideas.database.document.RootDocument;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class IdeaUpvote implements RootDocument {

    protected String id;
    protected String type;
    protected String ideaId;
    protected String userId;
    protected long timeUpvoted;

    public IdeaUpvote(String ideaId, String userId) {
        this.id = UUID.randomUUID().toString();
        this.type = "IdeaUpvote";
        this.ideaId = ideaId;
        this.userId = userId;
        this.timeUpvoted = Instant.now().getEpochSecond();
    }

    public String getPartitionKey() {
        return ideaId;
    }
}
