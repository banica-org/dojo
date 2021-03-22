package com.dojo.notifications.model.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class SlackClientManagerTest {

    private final static String INVALID_TOKEN = "token";

    private SlackClientManager slackClientManager;

    @Before
    public void init() {
        slackClientManager = new SlackClientManager();
    }

    @Test
    public void getSlackClientTest() {
        CustomSlackClient slackClient = slackClientManager.getSlackClient(INVALID_TOKEN);
        assertNotNull(slackClient);
    }
}
