package com.herokuapp.projectideas.database.document.post;

import com.herokuapp.projectideas.database.document.tag.Tag;
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
    protected List<Tag> tags;

    public Idea(
        String authorId,
        String authorUsername,
        String title,
        String content
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "Idea";
        this.ideaId = this.id;
        long now = Instant.now().getEpochSecond();
        this.timePosted = now;
        this.timeLastEdited = now;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
        this.tags = new ArrayList<>();
    }
}
