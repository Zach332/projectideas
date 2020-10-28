package com.herokuapp.projectideas.database.documents;

import java.util.UUID;

import com.azure.spring.data.cosmos.core.mapping.Container;

import org.springframework.data.annotation.Id;

@Container(containerName = "Ideas", ru = "400")
public class User {

    @Id
    private String id;
    private String username;
    private String email;

    public User(String username, String email) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
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
}
