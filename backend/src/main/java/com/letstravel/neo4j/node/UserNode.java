package com.letstravel.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("User")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserNode {

    @Id
    private Long id;

    private String email;

    @Relationship(type = "SUBSCRIBED_TO", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<TravelNode> subscribedTravels = new HashSet<>();
}
