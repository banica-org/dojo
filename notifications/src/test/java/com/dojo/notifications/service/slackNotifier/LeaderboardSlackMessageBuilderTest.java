package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.Attachment;
import com.hubspot.slack.client.models.blocks.Block;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardSlackMessageBuilderTest {

    private static final String USER_ID = "1";
    private static final String USER_NAME = "John";
    private static final String USER_EMAIL = "John@email";
    private static final long USER_SCORE = 100;
    private static final String MESSAGE = "Test";

    private static final String CHANNEL = "Channel";
    private static final String CONV_ID = "Conversation";

    private static final String PERSONAL_TITLE = "Your position in leaderboard has changed";
    private static final String COMMON_TITLE = "Leaderboard update";

    private static final int BLOCKS_EXPECTED_SIZE = 4;
    private static final int ATTACHMENTS_EXPECTED_SIZE = 1;

    private Leaderboard leaderboard;

    @Mock
    private CustomSlackClient slackClient;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetailsService userDetailsService;

    LeaderboardSlackMessageBuilder leaderboardSlackMessageBuilder;

    @Before
    public void init() {
        UserInfo userInfo = new UserInfo(USER_ID, USER_NAME);
        Participant participant = new Participant(userInfo, USER_SCORE);
        leaderboard = new Leaderboard(Collections.singletonList(participant));

        leaderboardSlackMessageBuilder = new LeaderboardSlackMessageBuilder();

        when(userDetailsService.getUserEmail(USER_ID)).thenReturn(USER_EMAIL);
        when(slackClient.getSlackUserId(USER_EMAIL)).thenReturn(CONV_ID);
    }

    @Test
    public void generatePersonalSlackMessageTest() {

        ChatPostMessageParams content = leaderboardSlackMessageBuilder.generateSlackContent(userDetailsService, userDetails, leaderboard, slackClient, CHANNEL, MESSAGE);

        List<Block> blocks = content.getBlocks();
        List<Attachment> attachments = content.getAttachments();

        assertEquals(ATTACHMENTS_EXPECTED_SIZE, attachments.size());
        assertEquals(BLOCKS_EXPECTED_SIZE, blocks.size());

        assertTrue(blocks.get(2).toString().contains(USER_NAME));
        assertTrue(blocks.get(2).toString().contains(String.valueOf(USER_SCORE)));
        assertTrue(blocks.get(2).toString().contains(PERSONAL_TITLE));
    }

    @Test
    public void generateCommonSlackMessageTest() {

        ChatPostMessageParams content = leaderboardSlackMessageBuilder.generateSlackContent(userDetailsService, leaderboard, slackClient, CHANNEL, MESSAGE);

        List<Block> blocks = content.getBlocks();
        List<Attachment> attachments = content.getAttachments();

        assertEquals(ATTACHMENTS_EXPECTED_SIZE, attachments.size());
        assertEquals(BLOCKS_EXPECTED_SIZE, blocks.size());

        assertTrue(blocks.get(2).toString().contains(USER_NAME));
        assertTrue(blocks.get(2).toString().contains(String.valueOf(USER_SCORE)));
        assertTrue(blocks.get(2).toString().contains(COMMON_TITLE));
    }
}
