package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Repository.search.UserSearchRepository;
import com.media.socialmedia.Security.JwtUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final UserSearchRepository repository;
    @Autowired
    public SearchService(UserSearchRepository repository) {
        this.repository = repository;
    }

    public List<UserSearchDocument> searchDocumentByEmail(String query){
        return repository.searchByEmail(query);
    }

    public Page<UserSearchDocument> fastSearchDocument(String query){
        return repository.searchByFullName(query,"",PageRequest.of(0,5));
    }

    public Page<UserSearchDocument> searchDocuments(String query,
                                                    String country,
                                                    int page){
        return repository.searchByFullName(query,country,PageRequest.of(page, 10));
    }
}
