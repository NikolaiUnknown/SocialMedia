package com.media.socialmedia.Controllers;

import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.DTO.PostResponseDTO;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.LikeService;
import com.media.socialmedia.Services.PostService;
import com.media.socialmedia.util.PostNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import({SecurityConfig.class, JwtCore.class})
class PostControllerTest {

    @MockitoBean
    private PostService postService;
    @MockitoBean
    private LikeService likeService;

    @Autowired
    private MockMvc mockMvc;

    private void setPrincipal(long id){
        JwtUserDetails userDetails = new JwtUserDetails(id,false,"");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getAllPostsByUserWithCorrectData() throws Exception {
        mockMvc.perform(get("/posts/users/{id}",0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPostWithCorrectData() throws Exception {
        PostResponseDTO post = new PostResponseDTO(0L,0L,"","",0);
        when(postService.getPost(0L)).thenReturn(post);
        mockMvc.perform(get("/posts/{id}",0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(post));
    }

    @Test
    void getNonExistPost() throws Exception {
        when(postService.getPost(0L)).thenThrow(new PostNotFoundException("Post not found!"));
        mockMvc.perform(get("/posts/{id}",0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Post not found!"));
    }

    @Test
    void createPostWithImage() throws Exception {
        setPrincipal(0L);
        MockMultipartFile file = new MockMultipartFile(
                "image","image.png", MediaType.IMAGE_PNG_VALUE,new byte[1]
        );
        mockMvc.perform(multipart(HttpMethod.POST,"/posts/")
                        .file(file)
                        .part(new MockPart("text","text".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("success"));
    }

    @Test
    void createPostWithoutImage() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(multipart(HttpMethod.POST,"/posts/")
                        .part(new MockPart("text","text".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("success"));
    }

    @Test
    void createPostWithServerError() throws Exception {
        setPrincipal(0L);
        MockMultipartFile file = new MockMultipartFile(
                "image","image.png", MediaType.IMAGE_PNG_VALUE,new byte[1]
        );
        doThrow(RuntimeException.class).when(postService).create(0L,"text",file);
        mockMvc.perform(multipart(HttpMethod.POST,"/posts/")
                        .file(file)
                        .part(new MockPart("text","text".getBytes())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Error saving file"));
    }

    @Test
    void createPostWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/posts/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void likePostWithCorrectData() throws Exception {
        setPrincipal(0L);
        when(likeService.like(0L, 0L)).thenReturn(1L);
        mockMvc.perform(post("/posts/like/{id}",0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("1"));
    }

    @Test
    void likeNonExistPost() throws Exception {
        setPrincipal(0L);
        when(likeService.like(0L, 0L))
                .thenThrow(new PostNotFoundException("Post not found!"));
        mockMvc.perform(post("/posts/like/{id}",0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Post not found!"));
    }

    @Test
    void likePostWithNonExistUser() throws Exception {
        setPrincipal(0L);
        when(likeService.like(0L, 0L))
                .thenThrow(new UsernameNotFoundException("User not found!"));
        mockMvc.perform(post("/posts/like/{id}",0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }
}