package com.media.socialmedia.Repository.search;

import com.media.socialmedia.Entity.search.UserSearchDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Set;

public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, Long> {
    Set<UserSearchDocument> searchByEmail(String email);
    @Query("{\"match_phrase_prefix\": {\"full_name\" : \"?0\"}}")
    Set<UserSearchDocument> searchByFullName(String fullName);
}
