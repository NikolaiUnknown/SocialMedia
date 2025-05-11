package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Repository.search.UserSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SearchService {

    private final UserSearchRepository repository;
    @Autowired
    public SearchService(UserSearchRepository repository) {
        this.repository = repository;
    }

    public Set<UserSearchDocument> searchDocuments(String query){
        if (query.contains("@")){
            return repository.searchByEmail(query);
        }
        else return repository.searchByFullName(query);
    }

}
