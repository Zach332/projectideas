package com.herokuapp.projectideas.database.document.post;

import lombok.*;

@Getter
@Setter
public abstract class Post {

    protected String id;
    protected String type;
    protected String ideaId;
    protected long timePosted;
    protected long timeLastEdited;
    protected String authorId;
    protected String authorUsername;
    protected String content;
}
