package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.notification.ParticipantNotification;
import com.dojo.notifications.model.notification.SenseiNotification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.notificationService.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DockerNotifierService {

    private final UserDetailsService userDetailsService;
    private final Map<NotifierType, NotificationService> notificationServices;

    @Autowired
    public DockerNotifierService(UserDetailsService userDetailsService, Collection<NotificationService> notificationServices) {
        this.userDetailsService = userDetailsService;
        this.notificationServices = notificationServices.stream()
                .collect(Collectors.toMap(NotificationService::getNotificationServiceTypeMapping, Function.identity()));
    }

    public void notifyParticipant(String username, Contest contest, Object object, String message, NotificationType type) {
        UserDetails userDetails = userDetailsService.getUserDetailsByUsername(username);
        for (NotifierType notifierType : contest.getPersonalNotifiers()) {
            notificationServices.get(notifierType)
                    .notify(userDetails, new ParticipantNotification(userDetailsService, object, userDetails, message, type), contest);
        }
    }

    public void notifySensei(Contest contest, Object object, String message, NotificationType type) {
        contest.getCommonNotificationsLevel().forEach((key, value) -> notificationServices.get(key)
                .notify(new SenseiNotification(userDetailsService, object, message, type), contest));
    }
}
