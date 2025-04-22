package com.media.socialmedia.DTO;

import com.media.socialmedia.Entity.User;
import lombok.Data;
import java.text.SimpleDateFormat;

@Data
public class UserDataResponseDTO {

    private final Long id;

    private String email;

    private final String firstname;

    private final String lastname;

    private String dateOfBirthday;

    private final String profilePicture;

    private String country;

    private final boolean isValid;

    private final boolean isPrivate;

    public UserDataResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.dateOfBirthday = new SimpleDateFormat("dd.MM.yyyy").format(user.getDateOfBirthday());
        this.profilePicture = user.getProfilePicture();
        this.country = user.getCountry();
        this.isValid = user.isValid();
        this.isPrivate = user.isPrivate();
    }
}
