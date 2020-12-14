package com.herokuapp.projectideas.database.document.post;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.View;
import lombok.*;

@Getter
@Setter
public abstract class Post {

    @JsonView(View.Get.class)
    protected String id;

    protected String type;
    protected String ideaId;

    @JsonView(View.Get.class)
    protected long timePosted;

    @JsonView(View.Get.class)
    protected long timeLastEdited;

    protected String authorId;

    @JsonView(View.Get.class)
    protected String authorUsername;

    @JsonView(View.Base.class)
    protected String content;
}
