package com.herokuapp.projectideas.database.repositories;

import java.util.List;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CosmosRepository<User, String> {
    @Query(value = "select * from c where c.type = \"User\"")
    List<User> findAll();
}
