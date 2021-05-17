package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.notification.ParticipantNotification;
import com.dojo.notifications.model.notification.SenseiNotification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.notificationService.EmailNotificationService;
import com.dojo.notifications.service.notificationService.NotificationService;
import com.dojo.notifications.service.notificationService.SlackNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class DockerNotifierServiceTest {

    private static final String USERNAME = "username";
    private static final String MESSAGE = "message";

    @Mock
    private UserDetails userDetails;

    @Mock
    private Contest contest;

    @Mock
    private Container container;

    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private SelectRequestService selectRequestService;
    @Mock
    private FlinkTableService flinkTableService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private SlackNotificationService slackNotificationService;

    private DockerNotifierService dockerNotifierService;

    @Before
    public void init() {
        List<NotificationService> notificationServices = new ArrayList<>();
        notificationServices.add(emailNotificationService);
        notificationServices.add(slackNotificationService);
        when(emailNotificationService.getNotificationServiceTypeMapping()).thenReturn(NotifierType.EMAIL);
        when(slackNotificationService.getNotificationServiceTypeMapping()).thenReturn(NotifierType.SLACK);
        dockerNotifierService = new DockerNotifierService(userDetailsService, selectRequestService, flinkTableService, notificationServices);
    }

    @Test
    public void notifyParticipantTest() {
        when(userDetailsService.getUserDetailsByUsername(USERNAME)).thenReturn(userDetails);
        Set<NotifierType> notifiers = new HashSet<>();
        notifiers.add(NotifierType.EMAIL);
        notifiers.add(NotifierType.SLACK);
        when(contest.getNotifiers()).thenReturn(notifiers);

        dockerNotifierService.notifyParticipant(USERNAME, contest, container, MESSAGE, NotificationType.CONTAINER);

        verify(slackNotificationService, times(1)).notify(eq(userDetails), any(ParticipantNotification.class), eq(contest));
        verify(emailNotificationService, times(1)).notify(eq(userDetails), any(ParticipantNotification.class), eq(contest));
    }

    @Test
    public void notifySenseiTest() {

        Set<NotifierType> notifiers = new HashSet<>();
        notifiers.add(NotifierType.EMAIL);
        notifiers.add(NotifierType.SLACK);
        when(contest.getNotifiers()).thenReturn(notifiers);

        dockerNotifierService.notifySensei(contest, container, MESSAGE, NotificationType.CONTAINER);

        verify(slackNotificationService, times(1)).notify(any(SenseiNotification.class), eq(contest));
        verify(emailNotificationService, times(1)).notify(any(SenseiNotification.class), eq(contest));
    }
}
