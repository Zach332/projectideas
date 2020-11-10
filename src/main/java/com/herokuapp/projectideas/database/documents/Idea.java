package com.herokuapp.projectideas.database.documents;

import java.time.Instant;
import java.util.UUID;

public class Idea {
    
    private String id;
    private String type;
    private String ideaId;
    private long timePosted;
    private long timeLastEdited;
    private String authorId;
    private String authorUsername;
    private String title;
    private String content;

    public Idea() { }

    public Idea(String authorId, String authorUsername, String title, String content) {
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
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getIdeaId() {
        return ideaId;
    }

    public long getTimePosted() {
        return timePosted;
    }

    public long getTimeLastEdited() {
        return timeLastEdited;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
