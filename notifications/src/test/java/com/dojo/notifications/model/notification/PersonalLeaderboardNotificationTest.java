package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.UserDetails;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class PersonalLeaderboardNotificationTest {

    private static final String CHANNEL = "channel";
    private static final String MESSAGE = "Mail message";

    @Mock
    private CustomSlackClient slackClient;

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
        leaderboard = new Leaderboard(new ArrayList<>());
        leaderboardNotification = new PersonalLeaderboardNotification(leaderboard, userDetails);
    }

    @Test
    public void getAsEmailNotificationTest() {
        MailContentBuilder mailContentBuilder = mock(LeaderboardMailMessageBuilder.class);
        when(mailContentBuilder.generateMailContent(anyMap())).thenReturn(MESSAGE);

        String actual = leaderboardNotification.getAsEmailNotification(mailContentBuilder);

        verify(mailContentBuilder, times(1)).generateMailContent(anyMap());
        assertEquals(actual, MESSAGE);
    }

    @Test
    public void getAsSlackNotificationTest() {
        SlackMessageBuilder slackMessageBuilder = mock(LeaderboardSlackMessageBuilder.class);
        when(slackMessageBuilder.generateSlackContent(userDetails, leaderboard, slackClient, CHANNEL))
                .thenReturn(chatPostMessageParams);

        leaderboardNotification.getAsSlackNotification(slackMessageBuilder, slackClient, CHANNEL);

        verify(slackMessageBuilder, times(1)).generateSlackContent(userDetails, leaderboard, slackClient, CHANNEL);
    }
}
