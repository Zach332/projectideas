package com.herokuapp.projectideas.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.herokuapp.projectideas.database.documents.Idea;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Database {

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer userContainer;
    private CosmosContainer postContainer;

    public Database(@Value("${azure.cosmos.uri}") String uri, @Value("${azure.cosmos.key}") String key, @Value("${azure.cosmos.database}") String databaseName) {
        client = new CosmosClientBuilder()
            .endpoint(uri)
            .key(key)
            .buildClient();
        database = client.getDatabase(databaseName);
        userContainer = database.getContainer("users");
        postContainer = database.getContainer("posts");
    }

    public Optional<User> findUser(String id) {
        return userContainer.queryItems("SELECT * FROM c WHERE c.id = '" + id + "'", new CosmosQueryRequestOptions(), User.class).stream().findFirst();
    }

    public Optional<User> findUserByEmail(String email) {
        return userContainer.queryItems("SELECT * FROM c WHERE c.email = '" + email + "'", new CosmosQueryRequestOptions(), User.class).stream().findFirst();
    }

    public List<Idea> findAllIdeas() {
        return postContainer.queryItems("SELECT * FROM c WHERE c.type = 'Idea'", new CosmosQueryRequestOptions(), Idea.class).stream().collect(Collectors.toList());
    }

    public Optional<Idea> findIdea(String id) {
        return postContainer.queryItems("SELECT * FROM c WHERE c.type = 'Idea' AND c.id = '" + id + "'", new CosmosQueryRequestOptions(), Idea.class).stream().findFirst();
    }

    public User createUser(User user) {
        userContainer.createItem(user);
        return userContainer.queryItems("SELECT * FROM c WHERE c.id = '" + user.getId() + "'", new CosmosQueryRequestOptions(), User.class).stream().findFirst().get();
    }

    public Idea createIdea(Idea idea) {
        postContainer.createItem(idea);
        return postContainer.queryItems("SELECT * FROM c WHERE c.type = 'Idea' AND c.id = '" + idea.getId() + "'", new CosmosQueryRequestOptions(), Idea.class).stream().findFirst().get();
    }

    public Idea updateIdea(String id, Idea idea) {
        postContainer.replaceItem(idea, id, new PartitionKey(id), new CosmosItemRequestOptions());
        return postContainer.queryItems("SELECT * FROM c WHERE c.id = '" + id + "'", new CosmosQueryRequestOptions(), Idea.class).stream().findFirst().get();
    }

    public User updateUser(String id, User user) {
        User oldUser = userContainer.queryItems("SELECT * FROM c WHERE c.id = '" + id + "'", new CosmosQueryRequestOptions(), User.class).stream().findFirst().get();

        // Handle username denormalization
        if (!user.getUsername().equals(oldUser.getUsername())) {
            ArrayList<Object> params = new ArrayList<Object>();
            params.add(user.getId());
            params.add(user.getUsername());
            
            // TODO: Return strings instead of the entire document
            List<Idea> partitionKeys = postContainer.queryItems("SELECT * FROM c WHERE c.authorId = '" + user.getId() + "'", new CosmosQueryRequestOptions(), Idea.class).stream()
                .collect(Collectors.toList());

            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

            for (Idea idea : partitionKeys) {
                PartitionKey partitionKey = new PartitionKey(idea.getId());
                options.setPartitionKey(partitionKey);
                postContainer.getScripts()
                    .getStoredProcedure("updateUsername")
                    .execute(params, options);
            }
        }

        userContainer.replaceItem(user, id, new PartitionKey(id), new CosmosItemRequestOptions());
        return userContainer.queryItems("SELECT * FROM c WHERE c.id = '" + id + "'", new CosmosQueryRequestOptions(), User.class).stream().findFirst().get();
    }

    public void deleteUser(String id) {
        userContainer.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
    }

    public void deleteIdea(String id) {
        postContainer.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
    }
}
