package com.letstravel.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchTravelRepository extends ElasticsearchRepository<TravelDocument, String> {

    java.util.List<TravelDocument> findByManagerId(Long managerId);
}
