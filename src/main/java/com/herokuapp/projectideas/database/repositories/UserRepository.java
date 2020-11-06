package com.herokuapp.projectideas.database.repositories;

import java.util.Optional;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CosmosRepository<User, String> {

    // @Override
    // @Query(value = "SELECT * FROM c WHERE c.type = 'User' AND c.id = @id")
    // Optional<User> findById(@Param("id") String id);

    // @Override
    // @Query(value = "SELECT * FROM c WHERE c.type = 'User'")
    // Iterable<User> findAll();

    // @Override
    // @Query(value = "DELETE FROM c WHERE c.type = 'User' AND c.id = @id")
    // void deleteById(@Param("id") String id);

    @Query(value = "SELECT * FROM c WHERE c.type = 'User' AND c.email = @email")
    Iterable<User> findByEmail(@Param("email") String email);
}
