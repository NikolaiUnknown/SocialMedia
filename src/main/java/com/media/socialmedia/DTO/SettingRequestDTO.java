package com.media.socialmedia.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingRequestDTO {
    @NotNull(message = "firstname is null!")
    @NotEmpty(message = "firstname is empty!")
    private String firstname;
    @NotNull(message = "lastname is null!")
    @NotEmpty(message = "lastname is empty!")
    private String lastname;
    @NotNull(message = "country isn't choose!")
    private String country;
    @NotNull
    private Date dateOfBirthday;

    private Boolean isPrivate;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String profilePicture;
}
