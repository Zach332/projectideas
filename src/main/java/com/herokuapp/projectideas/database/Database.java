package com.herokuapp.projectideas.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.herokuapp.projectideas.database.documents.Idea;

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

    public List<Idea> getIdeas() {
        Iterator<Idea> ideas = container.queryItems("SELECT * FROM c WHERE c.type = 'Idea'", new CosmosQueryRequestOptions(), Idea.class).iterator();
        List<Idea> ideasList = new ArrayList<Idea>();
        while (ideas.hasNext()) {
            Idea idea = ideas.next();
            ideasList.add(idea);
        }
        return ideasList;
    }
}
