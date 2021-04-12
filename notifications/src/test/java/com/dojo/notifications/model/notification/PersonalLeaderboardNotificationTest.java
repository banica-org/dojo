package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.UserDetails;
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
public class PersonalLeaderboardNotificationTest {

    private static final String CHANNEL = "channel";
    private static final String MESSAGE = "Mail message";
    private static final String LEADERBOARD_KEY = "leaderboard";
    private static final String USERDETAILS_KEY = "userDetails";
    public static final String MESSAGE_KEY = "message";
    private static final String REQUEST_MESSAGE = "Test";

    @Mock
    private CustomSlackClient slackClient;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

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
        leaderboardNotification = new PersonalLeaderboardNotification(userDetailsService, leaderboard, userDetails, REQUEST_MESSAGE);
    }

    @Test
    public void getAsEmailNotificationTest() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(LEADERBOARD_KEY, leaderboard.getParticipants());
        contextParams.put(MESSAGE_KEY, REQUEST_MESSAGE);
        contextParams.put(USERDETAILS_KEY, userDetails);

        MailMessageGenerator mailMessageGenerator = mock(LeaderboardMailMessageGenerator.class);
        when(mailMessageGenerator.generateMessage(contextParams)).thenReturn(MESSAGE);

        String actual = leaderboardNotification.getAsEmailNotification(mailMessageGenerator);

        verify(mailMessageGenerator, times(1)).generateMessage(contextParams);
        assertEquals(actual, MESSAGE);
    }

    @Test
    public void getAsSlackNotificationTest() {
        SlackMessageGenerator slackMessageGenerator = mock(LeaderboardSlackMessageGenerator.class);
        when(slackMessageGenerator.generateMessage(userDetailsService, userDetails, leaderboard, slackClient, CHANNEL, MESSAGE))
                .thenReturn(chatPostMessageParams);

        leaderboardNotification.getAsSlackNotification(slackMessageGenerator, slackClient, CHANNEL);

        verify(slackMessageGenerator, times(1)).generateMessage(userDetailsService, userDetails, leaderboard, slackClient, CHANNEL, REQUEST_MESSAGE);
    }
}
