package com.media.socialmedia.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name ="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String firstname;

    @NotNull
    private String lastname;

    @NotNull
    @Column(name = "date_of_birthday")
    private Date dateOfBirthday;

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
}
