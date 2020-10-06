package com.herokuapp.projectideas.database.repositories;

import com.herokuapp.projectideas.database.documents.User;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CosmosRepository<User, String> {
    
}
