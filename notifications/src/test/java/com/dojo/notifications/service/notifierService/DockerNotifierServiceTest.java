package com.dojo.notifications.service.notifierService;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.notification.NotifierRequestModel;
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

    private static final String MESSAGE = "message";
    private static final String ID = "1";

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

        when(userDetailsService.getUserDetailsById(ID)).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(ID);
        Set<NotifierType> notifiers = new HashSet<>();
        notifiers.add(NotifierType.EMAIL);
        notifiers.add(NotifierType.SLACK);
        when(contest.getNotifiers()).thenReturn(notifiers);

        when(container.getStatus()).thenReturn("running");
    }

    @Test
    public void executeRequestsParticipantTest() throws Exception {
        when(selectRequest.getReceivers()).thenReturn("1.");
        executeRequestsConditions();

        dockerNotifierService.executeRequests(contest, ID, container, MESSAGE);

        verify(selectRequestService, times(1)).getSpecificRequests(any(), any());
        verify(flinkTableService, times(1)).executeDockerQuery(eq(SELECT_REQUEST), any(), eq(ID));

        verifyParticipantNotificationsSent();
    }

    @Test
    public void executeRequestsCommonTest() throws Exception {
        SELECT_REQUEST.setReceivers("Common");
        when(selectRequest.getReceivers()).thenReturn("Common");
        executeRequestsConditions();

        dockerNotifierService.executeRequests(contest, ID, container, MESSAGE);

        verify(selectRequestService, times(1)).getSpecificRequests(any(), any());
        verify(flinkTableService, times(1)).executeDockerQuery(eq(SELECT_REQUEST), any(), eq(ID));

        verifyCommonNotificationsSent();
    }

    @Test
    public void executeRequestsAllTest() throws Exception {
        SELECT_REQUEST.setReceivers("Common");
        when(selectRequest.getReceivers()).thenReturn("1.,Common");
        executeRequestsConditions();

        dockerNotifierService.executeRequests(contest, ID, container, MESSAGE);

        verify(selectRequestService, times(1)).getSpecificRequests(any(), any());
        verify(flinkTableService, times(1)).executeDockerQuery(eq(SELECT_REQUEST), any(), eq(ID));

        verifyParticipantNotificationsSent();
        verifyCommonNotificationsSent();
    }

    @Test
    public void notifyParticipantTest() {
        NotifierRequestModel notifierRequestModel = new NotifierRequestModel(null, ID, contest, container, MESSAGE, NotificationType.CONTAINER, 1);
        dockerNotifierService.notifyParticipant(notifierRequestModel);
        verifyParticipantNotificationsSent();
    }

    @Test
    public void notifySenseiTest() {
        NotifierRequestModel notifierRequestModel = new NotifierRequestModel(null, ID, contest, container, MESSAGE, NotificationType.CONTAINER, 1);
        dockerNotifierService.notifySensei(notifierRequestModel);
        verifyCommonNotificationsSent();
    }

    private void executeRequestsConditions() throws Exception {
        when(selectRequestService.getSpecificRequests(any(), any())).thenReturn(Collections.singleton(SELECT_REQUEST));
        when(flinkTableService.executeDockerQuery(eq(SELECT_REQUEST), any(), eq(ID))).thenReturn(Collections.singletonList(ID));
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
