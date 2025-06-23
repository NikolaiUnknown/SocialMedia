package com.media.socialmedia.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {
    @NotNull
    private UUID chatId;
    @NotNull(message = "text is empty!")
    private String text;
}
