package com.dojo.notifications.model.client;

import com.hubspot.slack.client.SlackClientRuntimeConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class CustomSlackWebClientTest {

    private final static String INVALID_TOKEN = "token";
    private final static String INVALID_CONV_ID = "";
    private final static String INVALID_EMAIL = "email";

    private CustomSlackWebClient customSlackWebClient;

    @Before
    public void init() {
        SlackClientRuntimeConfig config = SlackClientRuntimeConfig.builder()
                .setTokenSupplier(() -> INVALID_TOKEN)
                .build();
        customSlackWebClient = new CustomSlackWebClient(config);
    }

    @Test
    public void getConversationIdTest() {
        String actual = customSlackWebClient.getConversationId(INVALID_EMAIL);

        assertEquals(INVALID_CONV_ID, actual);
    }
}
