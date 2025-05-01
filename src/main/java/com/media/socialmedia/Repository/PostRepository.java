package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Post getPostById(Long id);
    Set<Post> getPostsByUserId(long userId);
}
