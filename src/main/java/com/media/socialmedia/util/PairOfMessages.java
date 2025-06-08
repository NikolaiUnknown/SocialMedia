package com.media.socialmedia.util;

import com.media.socialmedia.DTO.MessageResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PairOfMessages {
    private MessageResponseDTO toSender;
    private MessageResponseDTO toRecipient;
}
