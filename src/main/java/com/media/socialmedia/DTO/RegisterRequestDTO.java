package com.media.socialmedia.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    @Email(message = "this is not email!")
    private String email;
    @Min(value = 6,message = "password must be greater than 6!")
    private String password;

}