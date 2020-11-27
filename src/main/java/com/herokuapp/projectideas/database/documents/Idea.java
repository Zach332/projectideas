package com.herokuapp.projectideas.database.documents;

import java.time.Instant;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class Idea {
    
    final private String id = UUID.randomUUID().toString();
    private String type;
    private String ideaId;
    private long timePosted;
    private long timeLastEdited;
    private String authorId;
    private String authorUsername;
    private String title;
    private String content;

    public Idea(String authorId, String authorUsername, String title, String content) {
        this.type = "Idea";
        this.ideaId = this.id;
        long now = Instant.now().getEpochSecond();
        this.timePosted = now;
        this.timeLastEdited = now;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
    }
}
