package com.dojo.notifications.service.notificationService;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;

public interface NotificationService {

    NotifierType getNotificationServiceTypeMapping();

    //Private message
    void notify(UserDetails userDetails, Notification notification, Contest contest);
    //Message in channel
    void notify(Notification notification, Contest contest);
}
