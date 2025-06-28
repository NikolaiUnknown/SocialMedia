package com.media.socialmedia.DTO;

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
    @NotNull(message = "date is null!")
    private Date dateOfBirth;
    private Boolean isPrivate;
}
