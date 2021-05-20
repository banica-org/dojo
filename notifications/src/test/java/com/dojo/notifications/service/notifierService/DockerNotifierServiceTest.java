package com.dojo.notifications.service.notifierService;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.notification.ParticipantNotification;
import com.dojo.notifications.model.notification.SenseiNotification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.FlinkTableService;
import com.dojo.notifications.service.SelectRequestService;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.notificationService.EmailNotificationService;
import com.dojo.notifications.service.notificationService.NotificationService;
import com.dojo.notifications.service.notificationService.SlackNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
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

    private final SelectRequest SELECT_REQUEST = new SelectRequest();

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
    private SelectRequest selectRequest;
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

        when(userDetailsService.getUserDetailsByUsername(USERNAME)).thenReturn(userDetails);
        Set<NotifierType> notifiers = new HashSet<>();
        notifiers.add(NotifierType.EMAIL);
        notifiers.add(NotifierType.SLACK);
        when(contest.getNotifiers()).thenReturn(notifiers);
    }

    @Test
    public void executeRequestsParticipantTest() throws Exception {
        when(selectRequestService.getSpecificRequests(any(), any())).thenReturn(Collections.singleton(SELECT_REQUEST));
        when(selectRequest.getReceivers()).thenReturn("1.");
        when(flinkTableService.executeDockerQuery(eq(SELECT_REQUEST), any())).thenReturn(Collections.singletonList(USERNAME));
        executeRequestsConditions();

        dockerNotifierService.executeRequests(contest, container, MESSAGE);

        verify(selectRequestService, times(1)).getSpecificRequests(any(), any());
        verify(flinkTableService, times(1)).executeDockerQuery(eq(SELECT_REQUEST), any());

        verifyParticipantNotificationsSent();
    }

    @Test
    public void executeRequestsCommonTest() throws Exception {
        SELECT_REQUEST.setReceivers("Common");
        when(selectRequestService.getSpecificRequests(any(), any())).thenReturn(Collections.singleton(SELECT_REQUEST));
        when(selectRequest.getReceivers()).thenReturn("Common");
        when(flinkTableService.executeDockerQuery(eq(SELECT_REQUEST), any())).thenReturn(Collections.singletonList(USERNAME));
        executeRequestsConditions();

        dockerNotifierService.executeRequests(contest, container, MESSAGE);

        verify(selectRequestService, times(1)).getSpecificRequests(any(), any());
        verify(flinkTableService, times(1)).executeDockerQuery(eq(SELECT_REQUEST), any());

        verifyCommonNotificationsSent();
    }

    @Test
    public void executeRequestsAllTest() throws Exception {
        SELECT_REQUEST.setReceivers("Common");
        when(selectRequestService.getSpecificRequests(any(), any())).thenReturn(Collections.singleton(SELECT_REQUEST));
        when(flinkTableService.executeDockerQuery(eq(SELECT_REQUEST), any())).thenReturn(Collections.singletonList(USERNAME));
        when(selectRequest.getReceivers()).thenReturn("1.,Common");
        executeRequestsConditions();

        dockerNotifierService.executeRequests(contest, container, MESSAGE);

        verify(selectRequestService, times(1)).getSpecificRequests(any(), any());
        verify(flinkTableService, times(1)).executeDockerQuery(eq(SELECT_REQUEST), any());

        verifyParticipantNotificationsSent();
        verifyCommonNotificationsSent();
    }

    @Test
    public void notifyParticipantTest() {
        dockerNotifierService.notifyParticipant(USERNAME, contest, container, MESSAGE, NotificationType.CONTAINER);
        verifyParticipantNotificationsSent();
    }

    @Test
    public void notifySenseiTest() {
        dockerNotifierService.notifySensei(contest, container, MESSAGE, NotificationType.CONTAINER);
        verifyCommonNotificationsSent();
    }

    private void executeRequestsConditions() throws Exception {
        when(selectRequestService.getRequestsForTable("docker_events")).thenReturn(Collections.singletonList(selectRequest));
        when(flinkTableService.executeDockerQuery(selectRequest, container)).thenReturn(Collections.singletonList(USERNAME));
    }

    private void verifyParticipantNotificationsSent() {
        verify(slackNotificationService, times(1)).notify(eq(userDetails), any(ParticipantNotification.class), eq(contest));
        verify(emailNotificationService, times(1)).notify(eq(userDetails), any(ParticipantNotification.class), eq(contest));

    }

    private void verifyCommonNotificationsSent() {
        verify(slackNotificationService, times(1)).notify(any(SenseiNotification.class), eq(contest));
        verify(emailNotificationService, times(1)).notify(any(SenseiNotification.class), eq(contest));
    }
}
