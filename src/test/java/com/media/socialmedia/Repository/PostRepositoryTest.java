package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findPostsIdByUserId() {
        Post post1 = new Post();
        post1.setUserId(0L);
        Post post2 = new Post();
        post2.setUserId(0L);
        postRepository.saveAll(List.of(post1,post2));
        assertEquals(Set.of(post1.getId(),post2.getId()),postRepository.findPostsIdByUserId(0L));
    }

    @Test
    void countPostLikesById() {
        Post post = new Post();
        postRepository.save(post);
        User user1 = new User();
        user1.setLikes(Set.of(post));
        User user2 = new User();
        user2.setLikes(Set.of(post));
        userRepository.saveAll(List.of(user1,user2));
        assertEquals(2L,postRepository.countPostLikesById(post.getId()));
    }

}