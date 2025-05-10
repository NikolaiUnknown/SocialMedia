package com.media.socialmedia.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateRequestDTO {
    @NotNull
    private String text;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String photo_url;
}
