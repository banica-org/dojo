package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.docker.Image;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Block;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ImageSlackMessageGeneratorTest {
    private static final String CONTENT_KEY = "content";
    private static final String MESSAGE_KEY = "message";
    private static final String MESSAGE = "Image message";
    private static final String IMAGE_TAG = "image tag";
    private static final String CHANNEL = "Channel";
    private static final int BLOCKS_EXPECTED_SIZE = 4;

    @Mock
    private Image image;

    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private CustomSlackClient slackClient;

    private ImageSlackMessageGenerator imageSlackMessageGenerator;

    @Before
    public void init() {
        imageSlackMessageGenerator = new ImageSlackMessageGenerator();
    }

    @Test
    public void getMessageGeneratorTypeMappingTest() {
        NotificationType expected = NotificationType.IMAGE;
        NotificationType actual = imageSlackMessageGenerator.getMessageGeneratorTypeMapping();
        assertEquals(expected, actual);
    }

    @Test
    public void generateMessageTest() {
        when(image.getImageTag()).thenReturn(IMAGE_TAG);
        when(image.getMessage()).thenReturn(MESSAGE);

        ChatPostMessageParams content = imageSlackMessageGenerator.generateMessage(userDetailsService, getContextParams(), slackClient, CHANNEL);

        List<Block> blocks = content.getBlocks();

        assertEquals(BLOCKS_EXPECTED_SIZE, blocks.size());

        assertTrue(blocks.get(1).toString().contains(MESSAGE));
        assertTrue(blocks.get(2).toString().contains(IMAGE_TAG));
        assertTrue(blocks.get(3).toString().contains(MESSAGE));
    }

    private Map<String, Object> getContextParams() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(MESSAGE_KEY, MESSAGE);
        contextParams.put(CONTENT_KEY, image);
        return contextParams;
    }
}
