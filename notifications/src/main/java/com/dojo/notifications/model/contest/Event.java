package com.dojo.notifications.model.contest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {
    private String roomName;
    private String gameName;
}
