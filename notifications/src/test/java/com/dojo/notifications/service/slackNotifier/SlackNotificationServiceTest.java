package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.enums.NotifierType;
import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.client.SlackClientManager;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class SlackNotificationServiceTest {

    private final static String TOKEN = "token";
    private final static String CONVERSATION_ID = "id";
    private final static String EMAIL = "email@email.com";

    @Mock
    private UserDetails userDetails;
    @Mock
    private Contest contest;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Notification notification;
    @Mock
    private CustomSlackClient slackClient;
    @Mock
    private SlackClientManager slackClientManager;

    @InjectMocks
    private SlackNotificationService slackNotificationService;

    @Before
    public void setUp() {
        when(contest.getSlackToken()).thenReturn(TOKEN);
        when(slackClientManager.getSlackClient(TOKEN)).thenReturn(slackClient);

        ChatPostMessageParams.Builder builder = ChatPostMessageParams.builder().addBlocks(Divider.builder().build());
        when(notification.convertToSlackNotification(any(), any())).thenReturn(builder);
    }

    @Test
    public void getNotificationServiceTypeMappingTest() {
        assertEquals(slackNotificationService.getNotificationServiceTypeMapping(), NotifierType.SLACK);
    }

    @Test
    public void notifyUserTest() {
        when(userDetails.getEmail()).thenReturn(EMAIL);
        when(slackClientManager.getSlackChannelForUser(EMAIL, slackClient)).thenReturn(CONVERSATION_ID);

        slackNotificationService.notify(userDetails, notification, contest);

        verify(slackClientManager, times(1)).getSlackClient(TOKEN);
        verify(slackClientManager, times(1)).getSlackChannelForUser(EMAIL, slackClient);
        verify(slackClient, times(1)).postMessage(any());
    }

    @Test
    public void notifyChannelTest() {
        when(contest.getSlackChannel()).thenReturn(CONVERSATION_ID);

        slackNotificationService.notify(notification, contest);

        verify(slackClientManager, times(1)).getSlackClient(TOKEN);
        verify(slackClient, times(1)).postMessage(any());
    }
}
