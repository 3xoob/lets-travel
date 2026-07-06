package com.letstravel.neo4j.repository;

import com.letstravel.neo4j.node.DestinationNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationNodeRepository extends Neo4jRepository<DestinationNode, String> {
}
