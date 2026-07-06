package com.letstravel.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Travel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TravelNode {

    @Id
    private Long id;

    private String title;

    @Relationship(type = "HAS_CATEGORY", direction = Relationship.Direction.OUTGOING)
    private CategoryNode category;

    @Relationship(type = "IN_DESTINATION", direction = Relationship.Direction.OUTGOING)
    private DestinationNode destination;

    @Relationship(type = "HAS_TAG", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<TagNode> tags = new HashSet<>();
}
