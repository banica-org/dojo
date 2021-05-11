package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.enums.NotificationType;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardSlackMessageGeneratorTest {

    private static final String USER_ID = "1";
    private static final String USER_NAME = "John";
    private static final String USER_EMAIL = "John@email";
    private static final long USER_SCORE = 100;
    private static final String MESSAGE = "Test";

    private static final String CHANNEL = "Channel";
    private static final String CONV_ID = "Conversation";

    private static final String COMMON_TITLE = "Leaderboard update";

    private static final String CONTENT_KEY = "content";
    private static final String MESSAGE_KEY = "message";
    private static final String USERDERAILS_KEY = "userDetails";

    private static final int BLOCKS_EXPECTED_SIZE = 3;
    private static final int ATTACHMENTS_EXPECTED_SIZE = 1;

    private Leaderboard leaderboard;

    @Mock
    private CustomSlackClient slackClient;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetailsService userDetailsService;

    LeaderboardSlackMessageGenerator leaderboardSlackMessageGenerator;

    @Before
    public void init() {
        UserInfo userInfo = new UserInfo(USER_ID, USER_NAME);
        Participant participant = new Participant(userInfo, USER_SCORE);
        TreeSet<Participant> participants = new TreeSet<>();
        participants.add(participant);
        leaderboard = new Leaderboard(participants);

        leaderboardSlackMessageGenerator = new LeaderboardSlackMessageGenerator();

        when(userDetailsService.getUserEmail(USER_ID)).thenReturn(USER_EMAIL);
        when(slackClient.getSlackUserId(USER_EMAIL)).thenReturn(CONV_ID);
    }

    @Test
    public void getMessageGeneratorTypeMappingTest() {
        NotificationType expected = NotificationType.LEADERBOARD;
        NotificationType actual = leaderboardSlackMessageGenerator.getMessageGeneratorTypeMapping();
        assertEquals(expected, actual);
    }

    @Test
    public void generatePersonalSlackMessageTest() {
        userDetails = new UserDetails();
        Map<String, Object> contextParams = getContextParams();
        contextParams.put(USERDERAILS_KEY, userDetails);

        ChatPostMessageParams content = leaderboardSlackMessageGenerator.generateMessage(userDetailsService, contextParams, slackClient, CHANNEL);

        List<Block> blocks = content.getBlocks();
        List<Attachment> attachments = content.getAttachments();

        assertEquals(ATTACHMENTS_EXPECTED_SIZE, attachments.size());
        assertEquals(BLOCKS_EXPECTED_SIZE, blocks.size());

        assertTrue(blocks.get(2).toString().contains(USER_NAME));
        assertTrue(blocks.get(2).toString().contains(String.valueOf(USER_SCORE)));
        assertTrue(blocks.get(2).toString().contains(COMMON_TITLE));
    }

    @Test
    public void generateCommonSlackMessageTest() {
        ChatPostMessageParams content = leaderboardSlackMessageGenerator.generateMessage(userDetailsService, getContextParams(), slackClient, CHANNEL);

        List<Block> blocks = content.getBlocks();
        List<Attachment> attachments = content.getAttachments();

        assertEquals(ATTACHMENTS_EXPECTED_SIZE, attachments.size());
        assertEquals(BLOCKS_EXPECTED_SIZE, blocks.size());

        assertTrue(blocks.get(2).toString().contains(USER_NAME));
        assertTrue(blocks.get(2).toString().contains(String.valueOf(USER_SCORE)));
        assertTrue(blocks.get(2).toString().contains(COMMON_TITLE));
    }

    private Map<String, Object> getContextParams() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(MESSAGE_KEY, MESSAGE);
        contextParams.put(CONTENT_KEY, leaderboard);
        return contextParams;
    }
}
