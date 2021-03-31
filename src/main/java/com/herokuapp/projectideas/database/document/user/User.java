package com.herokuapp.projectideas.database.document.user;

import com.herokuapp.projectideas.database.document.RootDocument;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class User implements RootDocument {

    protected String id;
    protected String type;
    protected String userId;
    protected String username;
    protected String email;
    protected long timeCreated;
    protected int unreadMessages;
    protected boolean admin;

    public User(String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.type = "User";
        this.userId = this.id;
        this.username = username;
        this.email = email;
        this.timeCreated = Instant.now().getEpochSecond();
        this.unreadMessages = 0;
        this.admin = false;
    }

    public String getPartitionKey() {
        return userId;
    }
}
