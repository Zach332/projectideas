package com.herokuapp.projectideas.database.documents;

import java.time.Instant;
import java.util.UUID;

import com.azure.spring.data.cosmos.core.mapping.Container;

import org.springframework.data.annotation.Id;

@Container(containerName = "Ideas", ru = "400")
public class Idea {
    
    @Id
    private String id;
    private long timePosted;
    private long timeLastEdited;
    private String authorUsername;
    private String title;
    private String content;

    public Idea(String authorUsername, String title, String content) {
        this.id = UUID.randomUUID().toString();
        long now = Instant.now().getEpochSecond();
        this.timePosted = now;
        this.timeLastEdited = now;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public long getTimePosted() {
        return timePosted;
    }

    public long getTimeLastEdited() {
        return timeLastEdited;
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
