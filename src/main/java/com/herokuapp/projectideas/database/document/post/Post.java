package com.herokuapp.projectideas.database.document.post;

import com.herokuapp.projectideas.database.document.Authorization;
import com.herokuapp.projectideas.database.document.RootDocument;
import lombok.*;

@Getter
@Setter
public abstract class Post implements RootDocument, Authorization {

    protected String id;
    protected String type;
    protected String ideaId;
    protected long timeCreated;
    protected long timeLastEdited;
    protected String authorId;
    protected String authorUsername;
    protected String content;

    public String getPartitionKey() {
        return ideaId;
    }

    public boolean userIsAuthorizedToView(String userId) {
        return true;
    }

    public boolean userIsAuthorizedToEdit(String userId) {
        return authorId.equals(userId);
    }
}
