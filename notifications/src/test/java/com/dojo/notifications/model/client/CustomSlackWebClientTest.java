package com.dojo.notifications.model.client;

import com.hubspot.algebra.Result;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CompletableFuture;

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

    @Test(expected = IllegalStateException.class)
    public void postMessageExceptionTest() {
        ChatPostMessageParams chatPostMessageParams = ChatPostMessageParams.builder().addBlocks(Divider.builder().build()).setChannelId("s").build();

        CompletableFuture<Result<ChatPostMessageResponse, SlackError>> actual = customSlackWebClient.postMessage(chatPostMessageParams);
        actual.join().unwrapOrElseThrow();
    }

    @Test
    public void getConversationIdTest() {
        String actual = customSlackWebClient.getConversationId(INVALID_EMAIL);

        assertEquals(INVALID_CONV_ID, actual);
    }
}
