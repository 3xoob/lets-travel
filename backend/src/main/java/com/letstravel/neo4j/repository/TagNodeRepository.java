package com.letstravel.neo4j.repository;

import com.letstravel.neo4j.node.TagNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagNodeRepository extends Neo4jRepository<TagNode, String> {
}
