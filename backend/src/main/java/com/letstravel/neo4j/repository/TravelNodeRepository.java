package com.letstravel.neo4j.repository;

import com.letstravel.neo4j.node.TravelNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelNodeRepository extends Neo4jRepository<TravelNode, Long> {
}
