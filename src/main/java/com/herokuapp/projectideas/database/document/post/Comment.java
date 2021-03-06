package com.herokuapp.projectideas.database.document.post;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
public class Comment extends Post {

    public Comment(
        String ideaId,
        String authorId,
        String authorUsername,
        String content
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "Comment";
        this.ideaId = ideaId;
        long now = Instant.now().getEpochSecond();
        this.timeCreated = now;
        this.timeLastEdited = now;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.content = content;
    }
}
