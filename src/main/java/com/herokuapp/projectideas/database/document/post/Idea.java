package com.herokuapp.projectideas.database.document.post;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.Tagged;
import com.herokuapp.projectideas.database.document.vote.Votable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Idea extends Post implements Votable, Tagged {

    protected String title;
    protected List<String> tags;
    protected int upvoteCount;
    protected boolean deleted;

    public Idea(
        String authorId,
        String authorUsername,
        String title,
        String content,
        List<String> tags
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "Idea";
        this.ideaId = this.id;
        long now = Instant.now().getEpochSecond();
        this.timeCreated = now;
        this.timeLastEdited = now;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.deleted = false;
        this.upvoteCount = 0;
    }

    /**
     * Set the appropriate fields before idea deletion.
     */
    public void delete() {
        deleted = true;
        authorId = null;
        authorUsername = null;
        content = "This idea has been deleted by its author.";
        tags = new ArrayList<>();
    }

    public void addUpvote() {
        upvoteCount += 1;
    }

    public void removeUpvote() {
        upvoteCount -= 1;
    }

    public boolean userHasUpvoted(String userId, Database database) {
        return database.userHasUpvotedIdea(ideaId, userId);
    }

    public boolean savedByUser(String userId, Database database) {
        return database.userHasSavedIdea(ideaId, userId);
    }
}
