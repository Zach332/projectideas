package com.herokuapp.projectideas.database.document.vote;

import com.herokuapp.projectideas.database.document.RootDocument;
import java.time.Instant;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public abstract class Upvote<T extends Votable> implements RootDocument {

    /**
     * Id of the user upvoting the document
     */
    protected String id;
    protected String type;
    protected long timeUpvoted;

    public Upvote(String userId) {
        this.id = userId;
        this.type = "Upvote";
        this.timeUpvoted = Instant.now().getEpochSecond();
    }
}
