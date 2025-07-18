package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User,Long> {
    User findUserByEmail(String email);
    Optional<User> findUserById(Long id);

    @Query("SELECT f.id FROM User u JOIN u.friends f WHERE u.id = :id")
    Set<Long> findFriendsById(Long id);

    @Query(value = "SELECT f.id FROM User u JOIN u.friendsOf f WHERE u.id= :id")
    Set<Long> findFriendsOfById(Long id);

    @Query(value = "SELECT b.id FROM User u JOIN u.blacklist b WHERE u.id= :id")
    Set<Long> findBlacklistById(Long id);

    @Query(value = "SELECT i.id FROM User u JOIN u.usersInvitedByMe i WHERE u.id= :id")
    Set<Long> findInvitesById(Long id);

    @Query(value = "SELECT i.id from User u JOIN u.usersInvitingMe i WHERE u.id= :id")
    Set<Long> findInvitesOfById(Long id);
}
