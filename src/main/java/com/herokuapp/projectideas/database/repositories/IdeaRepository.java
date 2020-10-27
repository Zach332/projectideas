package com.herokuapp.projectideas.database.repositories;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.herokuapp.projectideas.database.documents.Idea;

import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends CosmosRepository<Idea, String> {
    
}
