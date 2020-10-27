package com.herokuapp.projectideas.database.repositories;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CosmosRepository<User, String> {
    
}
