package com.herokuapp.projectideas.database.repositories;

import com.herokuapp.projectideas.database.documents.Idea;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends CosmosRepository<Idea, String> {
    
}
