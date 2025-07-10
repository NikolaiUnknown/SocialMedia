package com.media.socialmedia.Controllers;

import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Services.SearchService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@Import({SecurityConfig.class, JwtCore.class})
class SearchControllerTest {

    @MockitoBean
    private SearchService searchService;


    @MockitoBean
    @Qualifier("bucketSearch")
    private Bucket bucket;

    @Autowired
    private MockMvc mockMvc;

    private final String TOO_LARGE_QUERY = "000000000000000000000000000000";

    @Test
    void fastSearchDocumentsWithCorrectData() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        Page<UserSearchDocument> page = mock(Page.class);
        when(searchService.fastSearchDocument("test")).thenReturn(page);
        when(page.toList()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/search/fast")
                        .param("query","test"))
                .andExpect(status().isOk());
        verify(searchService).fastSearchDocument(any());
    }

    @Test
    void fastSearchDocumentsByEmailWithCorrectData() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        mockMvc.perform(get("/search/fast")
                        .param("query","test@test.com"))
                .andExpect(status().isOk());
        verify(searchService).searchDocumentByEmail(any());
    }

    @Test
    void fastSearchDocumentsWithTooManyRequests() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(false);
        mockMvc.perform(get("/search/fast")
                        .param("query",""))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$").value("Too many request, please wait..."));
    }

    @Test
    void fastSearchDocumentsWithTooLargeQuery() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        mockMvc.perform(get("/search/fast")
                .param("query",TOO_LARGE_QUERY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Too large request"));
    }

    @Test
    void searchDocumentsWithCorrectData() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        Page<UserSearchDocument> page = mock(Page.class);
        when(searchService.searchDocuments("test","",0)).thenReturn(page);
        when(page.toList()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/search/{page}",0)
                        .param("query","test"))
                .andExpect(status().isOk());
        verify(searchService).searchDocuments(any(),any(),eq(0));
    }

    @Test
    void searchDocumentsByEmailWithCorrectData() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        mockMvc.perform(get("/search/{page}",0)
                        .param("query","test@test.com"))
                .andExpect(status().isOk());
        verify(searchService).searchDocumentByEmail(any());
    }

    @Test
    void searchDocumentsWithTooManyRequests() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(false);
        mockMvc.perform(get("/search/{page}",0)
                        .param("query",""))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$").value("Too many request, please wait..."));
    }

    @Test
    void searchDocumentsWithTooLargeQuery() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        mockMvc.perform(get("/search/{page}",0)
                        .param("query",TOO_LARGE_QUERY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Too large request"));
    }
}