package com.herokuapp.projectideas.database.documents;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.View;

import lombok.*;

@Getter
@Setter
public abstract class Post {
    
    @JsonView(View.Get.class)
    String id = UUID.randomUUID().toString();
    String type;
    String ideaId;
    @JsonView(View.Get.class)
    long timePosted;
    @JsonView(View.Get.class)
    long timeLastEdited;
    String authorId;
    @JsonView(View.Get.class)
    String authorUsername;
    @JsonView(View.Base.class)
    String content;
}
