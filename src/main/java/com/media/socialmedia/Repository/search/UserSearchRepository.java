package com.media.socialmedia.Repository.search;

import com.media.socialmedia.Entity.search.UserSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, Long> {

    @Query("""
            {
                "term":{
                    "email.keyword": {
                        "value": "?0"
                    }
                }
            }
           """)
    List<UserSearchDocument> searchByEmail(String email);

    @Query("""
            {
                "bool":{
                    "must" : [
                    {
                        "term": {
                            "is_private": {
                                "value": false
                            }
                        }
                        },
                        {
                            "match_phrase_prefix": {
                                "full_name" : "?0"
                            }
                        }
                        ],
                    "should" : [
                    {
                        "term": {
                            "country.keyword": {
                                "value": "?1",
                                "boost": 3
                            }
                        }
                    }
                    ]
                }
            }
            """)
    Page<UserSearchDocument> searchByFullName(String fullName,
                                              String country,
                                              Pageable pageable);

    Optional<UserSearchDocument> searchById(Long id);
}
