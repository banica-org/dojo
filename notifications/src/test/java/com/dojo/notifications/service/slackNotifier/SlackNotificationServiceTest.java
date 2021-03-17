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

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class SlackNotificationServiceTest {

    private final static String TOKEN = "token";
    private final static String CONVERSATION_ID = "id";
    private final static String EMAIL = "email@email.com";
    private final static ChatPostMessageParams.Builder BUILDER = ChatPostMessageParams.builder();

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
        BUILDER.addBlocks(Divider.builder().build());
        when(notification.convertToSlackNotification(any(Function.class), eq(slackClient))).thenReturn(BUILDER);
    }

    @Test
    public void getNotificationServiceTypeMappingTest() {
        assertEquals(slackNotificationService.getNotificationServiceTypeMapping(), NotifierType.SLACK);
    }

    @Test
    public void notifyUserTest() {
        when(userDetails.getEmail()).thenReturn(EMAIL);
        when(slackClient.getConversationId(EMAIL)).thenReturn(CONVERSATION_ID);

        slackNotificationService.notify(userDetails, notification, contest);

        verify(slackClientManager, times(1)).getSlackClient(TOKEN);
        verify(slackClient, times(1)).getConversationId(EMAIL);
        verify(notification, times(1)).convertToSlackNotification(any(Function.class), eq(slackClient));
        verify(slackClient, times(1)).postMessage(BUILDER.build());
    }

    @Test
    public void notifyChannelTest() {
        when(contest.getSlackChannel()).thenReturn(CONVERSATION_ID);

        slackNotificationService.notify(notification, contest);

        verify(slackClientManager, times(1)).getSlackClient(TOKEN);
        verify(notification, times(1)).convertToSlackNotification(any(Function.class), eq(slackClient));
        verify(slackClient, times(1)).postMessage(BUILDER.build());
    }
}
