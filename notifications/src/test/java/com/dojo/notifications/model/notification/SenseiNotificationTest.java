package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.NotificationImpl;
import com.dojo.notifications.model.notification.SenseiNotification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.messageGenerator.mail.LeaderboardMailMessageGenerator;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import com.dojo.notifications.service.messageGenerator.slack.LeaderboardSlackMessageGenerator;
import com.dojo.notifications.service.messageGenerator.slack.SlackMessageGenerator;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class SenseiNotificationTest {

    private static final String CHANNEL = "channel";
    private static final String MESSAGE = "Mail message";
    private static final String CONTENT_KEY = "content";
    private static final String MESSAGE_KEY = "message";

    @Mock
    private CustomSlackClient slackClient;

    @Mock
    private UserDetailsService userDetailsService;

    private ChatPostMessageParams chatPostMessageParams;
    private Leaderboard leaderboard;

    private NotificationImpl notification;

    @Before
    public void init() {
        chatPostMessageParams = ChatPostMessageParams.builder()
                .addBlocks(Divider.builder().build())
                .setChannelId(CHANNEL)
                .build();
        leaderboard = new Leaderboard(new TreeSet<>());
        notification = new SenseiNotification(userDetailsService, leaderboard, MESSAGE, NotificationType.LEADERBOARD);
    }

    @Test
    public void getAsEmailNotificationTest() {
        Map<String, Object> contextParams = getContextParams();

        MailMessageGenerator mailMessageGenerator = mock(LeaderboardMailMessageGenerator.class);
        when(mailMessageGenerator.generateMessage(contextParams)).thenReturn(MESSAGE);

        String actual = notification.getAsEmailNotification(mailMessageGenerator);

        verify(mailMessageGenerator, times(1)).generateMessage(contextParams);
        assertEquals(actual, MESSAGE);
    }

    @Test
    public void getAsSlackNotificationTest() {
        SlackMessageGenerator slackMessageGenerator = mock(LeaderboardSlackMessageGenerator.class);
        Map<String, Object> contextParams = getContextParams();

        when(slackMessageGenerator.generateMessage(userDetailsService, contextParams, slackClient, CHANNEL))
                .thenReturn(chatPostMessageParams);

        notification.getAsSlackNotification(slackMessageGenerator, slackClient, CHANNEL);

        verify(slackMessageGenerator, times(1)).generateMessage(userDetailsService, contextParams, slackClient, CHANNEL);
    }

    private Map<String, Object> getContextParams() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(MESSAGE_KEY, MESSAGE);
        contextParams.put(CONTENT_KEY, leaderboard);
        return contextParams;
    }
}
