package com.herokuapp.projectideas.database.documents;

import java.time.Instant;
import java.util.UUID;

public class User {

    private String id;
    private String username;
    private String email;
    private long timeCreated;

    public User() { }

    public User(String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.timeCreated = Instant.now().getEpochSecond();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
