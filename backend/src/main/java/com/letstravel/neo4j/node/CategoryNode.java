package com.letstravel.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CategoryNode {
    @Id
    private String name;
}
