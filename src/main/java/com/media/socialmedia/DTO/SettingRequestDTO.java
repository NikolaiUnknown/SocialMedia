package com.media.socialmedia.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String profilePicture;
}
