package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.docker.Container;
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
public class ContainerSlackMessageGeneratorTest {

    private static final String CONTENT_KEY = "content";
    private static final String MESSAGE_KEY = "message";
    private static final String MESSAGE = "Container message";
    private static final String USERNAME = "username";
    private static final String STATUS = "running";
    private static final String CHANNEL = "Channel";
    private static final int BLOCKS_EXPECTED_SIZE = 4;

    @Mock
    private Container container;

    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private CustomSlackClient slackClient;

    private ContainerSlackMessageGenerator containerSlackMessageGenerator;

    @Before
    public void init() {
        containerSlackMessageGenerator = new ContainerSlackMessageGenerator();
    }

    @Test
    public void getMessageGeneratorTypeMappingTest() {
        NotificationType expected = NotificationType.CONTAINER;
        NotificationType actual = containerSlackMessageGenerator.getMessageGeneratorTypeMapping();
        assertEquals(expected, actual);
    }

    @Test
    public void generateMessageTest() {
        when(container.getUsername()).thenReturn(USERNAME);
        when(container.getStatus()).thenReturn(STATUS);

        ChatPostMessageParams content = containerSlackMessageGenerator.generateMessage(userDetailsService, getContextParams(), slackClient, CHANNEL);

        List<Block> blocks = content.getBlocks();

        assertEquals(BLOCKS_EXPECTED_SIZE, blocks.size());

        assertTrue(blocks.get(1).toString().contains(MESSAGE));
        assertTrue(blocks.get(2).toString().contains(USERNAME));
        assertTrue(blocks.get(3).toString().contains(STATUS));
    }

    private Map<String, Object> getContextParams() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(MESSAGE_KEY, MESSAGE);
        contextParams.put(CONTENT_KEY, container);
        return contextParams;
    }
}
