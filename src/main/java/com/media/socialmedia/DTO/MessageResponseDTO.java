package com.media.socialmedia.DTO;

import com.media.socialmedia.util.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO implements Comparable<MessageResponseDTO>{

    private Long id;

    private Date dateOfSend;

    private String text;

    private MessageType type;

    @Override
    public int compareTo(MessageResponseDTO msg) {
        if (this.dateOfSend.compareTo(msg.getDateOfSend()) != 0) {
            return this.getDateOfSend().compareTo(msg.getDateOfSend());
        }
        else {
            return this.getId().compareTo(msg.getId());
        }
    }

}
