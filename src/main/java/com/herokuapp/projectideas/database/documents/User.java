package com.herokuapp.projectideas.database.documents;

import java.util.UUID;

public class User {

    private String id;
    private String type;
    private String username;
    private String email;

    public User() { }

    public User(String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.type = "User";
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
