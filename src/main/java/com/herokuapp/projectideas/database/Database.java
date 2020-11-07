package com.herokuapp.projectideas.database;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.herokuapp.projectideas.database.documents.Idea;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Database {

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer container;

    public Database(@Value("${azure.cosmos.uri}") String uri, @Value("${azure.cosmos.key}") String key, @Value("${azure.cosmos.database}") String databaseName, @Value("${azure.cosmos.container}") String containerName) {
        client = new CosmosClientBuilder()
            .endpoint(uri)
            .key(key)
            .buildClient();
        database = client.getDatabase(databaseName);
        container = database.getContainer(containerName);
    }

    public List<User> findAllUsers() {
        return listQuery("SELECT * FROM c WHERE c.type = 'User'", User.class);
    }

    public Optional<User> findUser(String id) {
        return optionalQuery("SELECT * FROM c WHERE c.type = 'User' AND c.id = " + id, User.class);
    }

    public List<Idea> findAllIdeas() {
        return listQuery("SELECT * FROM c WHERE c.type = 'Idea'", Idea.class);
    }

    public Optional<Idea> findIdea(String id) {
        return optionalQuery("SELECT * FROM c WHERE c.type = 'User' AND c.id = " + id, Idea.class);
    }

    private <T> List<T> listQuery(String query, Class<T> object) {
        return container.queryItems(query, new CosmosQueryRequestOptions(), object).stream().collect(Collectors.toList());
    }

    private <T> Optional<T> optionalQuery(String query, Class<T> object) {
        return container.queryItems(query, new CosmosQueryRequestOptions(), object).stream().findFirst();
    }
}
