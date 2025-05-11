package com.media.socialmedia.Controllers;

import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping()
    public Set<UserSearchDocument> searchDocuments(@RequestParam("query") String query){
        return searchService.searchDocuments(query);
    }
}
