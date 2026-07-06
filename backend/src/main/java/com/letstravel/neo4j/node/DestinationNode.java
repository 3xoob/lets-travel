package com.letstravel.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Destination")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DestinationNode {
    @Id
    private String key;  // "city:country"
    private String city;
    private String country;
    private String region; // europe, asia, americas, africa, oceania, middle_east
}
