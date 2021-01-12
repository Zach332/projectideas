package com.herokuapp.projectideas.database.document.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class User {

    protected String id;
    protected String type;
    protected String userId;
    protected String username;
    protected String email;
    protected long timeCreated;
    protected boolean admin;
    protected List<String> postedIdeaIds;
    protected List<String> savedIdeaIds;
    protected List<String> joinedProjectIds;

    public User(String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.type = "User";
        this.userId = this.id;
        this.username = username;
        this.email = email;
        this.timeCreated = Instant.now().getEpochSecond();
        this.admin = false;
        this.postedIdeaIds = new ArrayList<String>();
        this.savedIdeaIds = new ArrayList<String>();
        this.joinedProjectIds = new ArrayList<String>();
    }
}
