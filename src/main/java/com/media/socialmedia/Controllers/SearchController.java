package com.media.socialmedia.Controllers;

import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.SearchService;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final Bucket bucket;
    @Autowired
    public SearchController(SearchService searchService,
                            @Qualifier(value = "bucketSearch") Bucket bucket) {
        this.searchService = searchService;
        this.bucket = bucket;
    }
    @GetMapping("/fast")
    public ResponseEntity<List<UserSearchDocument>> fastSearchDocuments(@RequestParam("query") String query){
        if (!bucket.tryConsume(1)){
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many request, please wait...");
        }
        if (query.length() >= 30){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Too large request");
        }
        if (query.contains("@")){
            return ResponseEntity.ok(searchService.searchDocumentByEmail(query));
        }
        else {
            return ResponseEntity.ok(searchService.fastSearchDocument(query).toList());
        }
    }

    @GetMapping("/{page}")
    public ResponseEntity<List<UserSearchDocument>> searchDocuments(@AuthenticationPrincipal JwtUserDetails userDetails,
                                                    @PathVariable("page") int page,
                                                    @RequestParam("query") String query){
        if (!bucket.tryConsume(1)){
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many request, please wait...");
        }
        if (query.length() >= 30){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too large request");
        }
        if (query.contains("@")){
            return ResponseEntity.ok(searchService.searchDocumentByEmail(query));
        }
        else {
            String country = "";
            if (userDetails != null) {
                country = userDetails.getCountry();
            }
            return ResponseEntity.ok(searchService.searchDocuments(query,country,page).toList());
        }
    }
    @ExceptionHandler
    private ResponseEntity<String> handle(ResponseStatusException exception){
        return new ResponseEntity<>(exception.getReason(), exception.getStatusCode());
    }
}
