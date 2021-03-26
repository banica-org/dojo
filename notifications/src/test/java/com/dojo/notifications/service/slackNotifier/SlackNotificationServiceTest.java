package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.client.SlackClientManager;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class SlackNotificationServiceTest {

    private final static String TOKEN = "token";
    private final static String CHANNEL = "id";
    private final static String EMAIL = "email@email.com";

    private ChatPostMessageParams chatPostMessageParams;

    @Mock
    private UserDetails userDetails;
    @Mock
    private Contest contest;
    @Mock
    private Notification notification;
    @Mock
    private CustomSlackClient slackClient;
    @Mock
    private SlackClientManager slackClientManager;
    @Mock
    private SlackMessageBuilder slackMessageBuilder;

    @InjectMocks
    private SlackNotificationService slackNotificationService;

    @Before
    public void setUp() {
        when(contest.getSlackToken()).thenReturn(TOKEN);
        when(slackClientManager.getSlackClient(TOKEN)).thenReturn(slackClient);
        chatPostMessageParams = ChatPostMessageParams.builder().addBlocks(Divider.builder().build()).setChannelId(CHANNEL).build();
    }

    @Test
    public void getNotificationServiceTypeMappingTest() {
        assertEquals(slackNotificationService.getNotificationServiceTypeMapping(), NotifierType.SLACK);
    }

    @Test
    public void notifyUserTest() {
        when(userDetails.getEmail()).thenReturn(EMAIL);
        when(slackClient.getConversationId(EMAIL)).thenReturn(CHANNEL);
        when(notification.getAsSlackNotification(slackMessageBuilder, slackClient, CHANNEL)).thenReturn(chatPostMessageParams);

        slackNotificationService.notify(userDetails, notification, contest);

        verify(slackClientManager, times(1)).getSlackClient(TOKEN);
        verify(slackClient, times(1)).getConversationId(EMAIL);
        verify(notification, times(1)).getAsSlackNotification(slackMessageBuilder, slackClient, CHANNEL);
        verify(slackClient, times(1)).postMessage(chatPostMessageParams);
    }

    @Test
    public void notifyChannelTest() {
        when(contest.getSlackChannel()).thenReturn(CHANNEL);
        when(notification.getAsSlackNotification(slackMessageBuilder, slackClient, CHANNEL)).thenReturn(chatPostMessageParams);

        slackNotificationService.notify(notification, contest);

        verify(slackClientManager, times(1)).getSlackClient(TOKEN);
        verify(notification, times(1)).getAsSlackNotification(slackMessageBuilder, slackClient, CHANNEL);
        verify(slackClient, times(1)).postMessage(chatPostMessageParams);
    }
}
