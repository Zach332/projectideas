package com.herokuapp.projectideas.database.document.vote;

import com.herokuapp.projectideas.database.document.post.Idea;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class IdeaUpvote extends Upvote<Idea> {

    protected String ideaId;

    public IdeaUpvote(String ideaId, String userId) {
        super(userId);
        this.type = "IdeaUpvote";
        this.ideaId = ideaId;
    }

    public String getPartitionKey() {
        return ideaId;
    }
}
