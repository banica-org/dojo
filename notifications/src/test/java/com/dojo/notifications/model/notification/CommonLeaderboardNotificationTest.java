package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.emailNotifier.LeaderboardMailMessageBuilder;
import com.dojo.notifications.service.emailNotifier.MailContentBuilder;
import com.dojo.notifications.service.slackNotifier.LeaderboardSlackMessageBuilder;
import com.dojo.notifications.service.slackNotifier.SlackMessageBuilder;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class CommonLeaderboardNotificationTest {

    private static final String CHANNEL = "channel";
    private static final String MESSAGE = "Mail message";
    private static final String LEADERBOARD_KEY = "leaderboard";
    private static final String MESSAGE_KEY = "message";

    @Mock
    private CustomSlackClient slackClient;

    @Mock
    private UserDetailsService userDetailsService;

    private ChatPostMessageParams chatPostMessageParams;
    private Leaderboard leaderboard;

    private LeaderboardNotification leaderboardNotification;

    @Before
    public void init() {
        chatPostMessageParams = ChatPostMessageParams.builder()
                .addBlocks(Divider.builder().build())
                .setChannelId(CHANNEL)
                .build();
        leaderboard = new Leaderboard(new TreeSet<>());
        leaderboardNotification = new CommonLeaderboardNotification(userDetailsService, leaderboard, MESSAGE);
    }

    @Test
    public void getAsEmailNotificationTest() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(MESSAGE_KEY, MESSAGE);
        contextParams.put(LEADERBOARD_KEY, leaderboard.getParticipants());

        MailContentBuilder mailContentBuilder = mock(LeaderboardMailMessageBuilder.class);
        when(mailContentBuilder.generateMailContent(contextParams)).thenReturn(MESSAGE);

        String actual = leaderboardNotification.getAsEmailNotification(mailContentBuilder);

        verify(mailContentBuilder, times(1)).generateMailContent(contextParams);
        assertEquals(actual, MESSAGE);
    }

    @Test
    public void getAsSlackNotificationTest() {
        SlackMessageBuilder slackMessageBuilder = mock(LeaderboardSlackMessageBuilder.class);
        when(slackMessageBuilder.generateSlackContent(userDetailsService, leaderboard, slackClient, CHANNEL, MESSAGE))
                .thenReturn(chatPostMessageParams);

        leaderboardNotification.getAsSlackNotification(slackMessageBuilder, slackClient, CHANNEL);

        verify(slackMessageBuilder, times(1)).generateSlackContent(userDetailsService, leaderboard, slackClient, CHANNEL, MESSAGE);
    }
}
