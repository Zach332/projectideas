package com.herokuapp.projectideas.database.documents;

import java.util.UUID;

import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;

import org.springframework.data.annotation.Id;

@Document(collection = "Users")
public class User {

    @Id
    private String id;
    private String username;
    private String email;

    public User() { }

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
