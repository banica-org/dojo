package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.Attachment;
import com.hubspot.slack.client.models.actions.Action;
import com.hubspot.slack.client.models.actions.ActionType;
import com.hubspot.slack.client.models.blocks.Block;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardSlackMessageBuilderTest {

    private static final String CHANNEL = "Channel";
    private static final String TITLE = "Title";
    private static final String NAMES = "Names";
    private static final String SCORES = "Scores";
    private static final int BLOCKS_EXPECTED_SIZE = 3;
    private static final int ATTACHMENTS_EXPECTED_SIZE = 1;

    LeaderboardSlackMessageBuilder leaderboardSlackMessageBuilder;

    @Before
    public void init() {
        leaderboardSlackMessageBuilder = new LeaderboardSlackMessageBuilder();
    }

    @Test
    public void generateSlackMessageTest() {
        Text names = Text.of(TextType.MARKDOWN, NAMES);
        Text scores = Text.of(TextType.MARKDOWN, SCORES);

        ChatPostMessageParams.Builder content = leaderboardSlackMessageBuilder.generateSlackContent(TITLE, names, scores);
        content.setChannelId(CHANNEL);
        List<Block> blocks = content.build().getBlocks();
        List<Attachment> attachments = content.build().getAttachments();

        assertEquals(ATTACHMENTS_EXPECTED_SIZE, attachments.size());
        assertEquals(BLOCKS_EXPECTED_SIZE, blocks.size());
        assertTrue(blocks.get(1).toString().contains(NAMES));
        assertTrue(blocks.get(1).toString().contains(SCORES));
    }
}
