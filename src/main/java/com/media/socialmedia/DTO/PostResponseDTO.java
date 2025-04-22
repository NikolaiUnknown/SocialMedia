package com.media.socialmedia.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDTO {
    private long id;
    private long userId;
    private String photoUrl;
    private String text;
    private long countOfLike;
}
