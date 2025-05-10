package com.media.socialmedia.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String email;

    private String firstname;

    private String lastname;

    private String dateOfBirthday;

    private String profilePicture;

    private String country;

    private boolean isValid;

    private boolean isPrivate;

    private boolean isBlocked;

}
