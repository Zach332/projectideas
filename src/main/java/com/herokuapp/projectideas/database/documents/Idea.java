package com.herokuapp.projectideas.database.documents;

import java.util.UUID;

import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;

import org.springframework.data.annotation.Id;

@Document(collection = "Ideas")
public class Idea {
    
    @Id
    private String id;
    private String authorId;
    private String content;

    public Idea(String authorId, String content) {
        this.id = UUID.randomUUID().toString();
        this.authorId = authorId;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }
}
