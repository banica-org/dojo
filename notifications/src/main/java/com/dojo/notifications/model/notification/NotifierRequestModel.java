package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotifierRequestModel {

    private final String receivers;
    private final String queriedId;
    private final Contest contest;
    private final Object object;
    private final String message;
    private final NotificationType notificationType;
    private final int requestId;

}
