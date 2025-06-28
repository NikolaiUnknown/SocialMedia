package com.media.socialmedia.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name ="users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String firstname;

    private String lastname;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "profile_picture")
    private String profilePicture;

    private String country;

    private boolean isValid;

    private boolean isPrivate;

    private boolean isAdmin;

    private boolean isBlocked;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "likes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private Set<Post> likes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @ManyToMany(mappedBy = "friends")
    private Set<User> friendsOf = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "invites",
            joinColumns = @JoinColumn(name = "user_from"),
            inverseJoinColumns = @JoinColumn(name = "user_to")
    )
    private Set<User> usersInvitedByMe = new HashSet<>();
    @ManyToMany(mappedBy = "usersInvitedByMe")
    private Set<User> usersInvitingMe = new HashSet<>();
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "blacklist",
            joinColumns = @JoinColumn(name = "user_from"),
            inverseJoinColumns = @JoinColumn(name = "user_to")
    )
    private Set<User> blacklist = new HashSet<>();

    @ManyToMany(mappedBy = "members")
    private Set<Chat> chats = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
