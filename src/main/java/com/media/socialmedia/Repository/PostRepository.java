package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Post;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Post getPostById(Long id);

    @Modifying
    @Query("SELECT p.id FROM Post p where p.userId=:userId")
    Set<Long> findPostsIdByUserId(long userId);
}
