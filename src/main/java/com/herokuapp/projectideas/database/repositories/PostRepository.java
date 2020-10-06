package com.herokuapp.projectideas.database.repositories;

import com.herokuapp.projectideas.database.documents.Post;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends CosmosRepository<Post, String> {
    
}
