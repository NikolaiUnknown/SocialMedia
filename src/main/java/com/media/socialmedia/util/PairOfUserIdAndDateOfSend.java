package com.media.socialmedia.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PairOfUserIdAndDateOfSend {

    private Long userId;

    private Date dateOfSend;
}
