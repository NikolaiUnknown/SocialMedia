package com.media.socialmedia.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {
    @NotNull
    private Long recipientId;
    @NotNull(message = "text is empty!")
    private String text;
}
