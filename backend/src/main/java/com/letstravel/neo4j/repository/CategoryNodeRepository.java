package com.letstravel.neo4j.repository;

import com.letstravel.neo4j.node.CategoryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryNodeRepository extends Neo4jRepository<CategoryNode, String> {
}
