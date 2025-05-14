package com.media.socialmedia.Entity.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "users")
public class UserSearchDocument {
    @Field(type = FieldType.Long)
    private Long id;
    @Field(type = FieldType.Text)
    private String email;
    @Field(name = "full_name", type = FieldType.Text)
    private String fullName;
    @Field(name = "profile_picture", type = FieldType.Text)
    private String profilePicture;
    @Field(name = "is_private", type = FieldType.Boolean)
    private boolean isPrivate;
}

