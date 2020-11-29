package com.herokuapp.projectideas.database.documents;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.View;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Idea {
    
    @JsonView(View.Get.class)
    private String id;
    private String type;
    private String ideaId;
    @JsonView(View.Get.class)
    private long timePosted;
    @JsonView(View.Get.class)
    private long timeLastEdited;
    private String authorId;
    @JsonView(View.Get.class)
    private String authorUsername;
    @JsonView(View.Base.class)
    private String title;
    @JsonView(View.Base.class)
    private String content;

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
}
