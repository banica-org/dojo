package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.emailNotifier.LeaderboardMailMessageBuilder;
import com.dojo.notifications.service.emailNotifier.MailContentBuilder;
import com.dojo.notifications.service.slackNotifier.LeaderboardSlackMessageBuilder;
import com.dojo.notifications.service.slackNotifier.SlackMessageBuilder;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class PersonalLeaderboardNotificationTest {

    private static final long ID = 1;
    private static final String SLACK_ID = "Id";
    private static final String NAME = "John";
    private static final String EMAIL = "John@email";
    private static final long SCORE = 100;
    private static final String MESSAGE = "Mail message";

    @Mock
    private CustomSlackClient slackClient;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetailsService userDetailsService;

    private List<User> leaderboard;
    private LeaderboardNotification leaderboardNotification;

    @Before
    public void init() {
        UserInfo userInfo = new UserInfo(ID, NAME, null);
        User user = new User(userInfo, SCORE);

        leaderboard = Collections.singletonList(user);
        leaderboardNotification = new PersonalLeaderboardNotification(leaderboard, userDetailsService, userDetails);
    }

    @Test
    public void buildLeaderboardNamesTest() {
        when(userDetailsService.getUserEmail(ID)).thenReturn(EMAIL);
        when(slackClient.getSlackUserId(EMAIL)).thenReturn(SLACK_ID);

        leaderboardNotification.buildLeaderboardNames(slackClient);

        verify(userDetailsService, times(1)).getUserEmail(ID);
        verify(slackClient, times(1)).getSlackUserId(EMAIL);
    }

    @Test
    public void buildLeaderboardScoresTest() {
        when(userDetails.getId()).thenReturn(ID);

        leaderboardNotification.buildLeaderboardScores();

        verify(userDetails, times(1)).getId();
        assertEquals(ID, userDetails.getId());
    }

    @Test
    public void convertToEmailNotificationTest() {
        MailContentBuilder mailContentBuilder = mock(LeaderboardMailMessageBuilder.class);
        when(mailContentBuilder.generateMailContent(anyMap())).thenReturn(MESSAGE);

        String actual = leaderboardNotification.convertToEmailNotification(mailContentBuilder);

        verify(mailContentBuilder, times(1)).generateMailContent(anyMap());
        assertEquals(actual, MESSAGE);
    }

    @Test
    public void convertToSlackNotificationTest() {
        when(userDetailsService.getUserEmail(ID)).thenReturn(EMAIL);
        when(slackClient.getSlackUserId(EMAIL)).thenReturn(SLACK_ID);

        SlackMessageBuilder slackMessageBuilder = mock(LeaderboardSlackMessageBuilder.class);
        when(slackMessageBuilder.generateSlackContent(anyString(), any(Text.class), any(Text.class)))
                .thenReturn(any(ChatPostMessageParams.Builder.class));

        leaderboardNotification.convertToSlackNotification(slackMessageBuilder, slackClient);

        verify(slackMessageBuilder, times(1)).generateSlackContent(anyString(), any(Text.class), any(Text.class));
    }
}
