package com.herokuapp.projectideas.database.document.post;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Idea extends Post {

    protected String title;
    protected List<String> tags;
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
        this.timePosted = now;
        // TODO: Keep this field updated
        this.timeLastEdited = now;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
        this.tags = tags;
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
}
