package com.media.socialmedia.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingRequestDTO {
    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    @NotNull
    private String country;
    @NotNull
    private Date dateOfBirthday;
    @NotNull
    private Boolean isPrivate;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String profilePicture;
}
