package com.herokuapp.projectideas.database.query;

import com.azure.cosmos.CosmosContainer;
import lombok.*;

@Getter
public class ProjectIdeasCosmosContainer {

    private CosmosContainer container;
    private String partitionKey;

    public ProjectIdeasCosmosContainer(
        CosmosContainer container,
        String partitionKey
    ) {
        this.container = container;
        this.partitionKey = partitionKey;
    }
}
