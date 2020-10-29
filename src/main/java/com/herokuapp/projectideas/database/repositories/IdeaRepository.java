package com.herokuapp.projectideas.database.repositories;

import java.util.List;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.herokuapp.projectideas.database.documents.Idea;

import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends CosmosRepository<Idea, String> {
    @Query(value = "select * from c where c.type = \"Idea\"")
    List<Idea> findAll();
}
