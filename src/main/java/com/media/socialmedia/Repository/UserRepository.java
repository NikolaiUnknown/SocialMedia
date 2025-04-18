package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User,Long> {
    User findUserByEmail(String email);
    @Modifying
    @Query("update User u set u.firstname = :firstname, u.lastname = :lastname, u.dateOfBirthday = :dateOfBirthday, u.country = :country, u.profilePicture = :profilePicture, u.isValid = true where u.id = :userId")
    void setUserInfoById(String firstname, String lastname, Date dateOfBirthday, String country, String profilePicture, Long userId);

    User findUserById(Long id);
}
