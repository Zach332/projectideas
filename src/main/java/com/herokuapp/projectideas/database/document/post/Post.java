package com.herokuapp.projectideas.database.document.post;

import com.herokuapp.projectideas.database.document.RootDocument;
import lombok.*;

@Getter
@Setter
public abstract class Post implements RootDocument {

    protected String id;
    protected String type;
    protected String ideaId;
    protected long timePosted;
    protected long timeLastEdited;
    protected String authorId;
    protected String authorUsername;
    protected String content;
}
