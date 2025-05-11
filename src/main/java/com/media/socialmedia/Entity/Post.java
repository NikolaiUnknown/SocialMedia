package com.media.socialmedia.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Table(name = "posts")
@Entity
@EqualsAndHashCode
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    @NotNull
    private long userId;
    @Column(name = "photo_url")
    private String photoUrl;
    private String text;

    @ManyToMany(mappedBy = "likes")
    private Set<User> likes = new HashSet<>();
}
