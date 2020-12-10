package com.herokuapp.projectideas.database.document;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.View;
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

    @JsonView(View.Base.class)
    protected String username;

    @JsonView(View.Base.class)
    protected String email;

    @JsonView(View.Get.class)
    protected long timeCreated;

    @JsonView(View.Get.class)
    protected boolean admin;

    protected List<String> savedIdeaIds;

    public User(String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.timeCreated = Instant.now().getEpochSecond();
        this.admin = false;
        this.savedIdeaIds = new ArrayList<String>();
    }
}
