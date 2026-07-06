package com.letstravel.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Tag")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TagNode {
    @Id
    private String name;
}
