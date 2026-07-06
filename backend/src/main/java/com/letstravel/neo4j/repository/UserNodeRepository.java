package com.letstravel.neo4j.repository;

import com.letstravel.neo4j.node.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNodeRepository extends Neo4jRepository<UserNode, Long> {
    Optional<UserNode> findByEmail(String email);
}
