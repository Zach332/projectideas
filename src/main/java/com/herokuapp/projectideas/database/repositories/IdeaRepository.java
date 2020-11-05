package com.herokuapp.projectideas.database.repositories;

import java.util.Optional;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.herokuapp.projectideas.database.documents.Idea;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends CosmosRepository<Idea, String> {

    @Override
    @Query(value = "SELECT * FROM c WHERE c.type = 'Idea' AND c.id = @id")
    Optional<Idea> findById(@Param("id") String id);

    @Override
    @Query(value = "SELECT * FROM c WHERE c.type = 'Idea'")
    Iterable<Idea> findAll();

    @Override
    @Query(value = "DELETE FROM c WHERE c.type = 'Idea' AND c.id = @id")
    void deleteById(@Param("id") String id);
}
