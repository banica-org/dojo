package com.dojo.notifications.client;

import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.SlackClientRuntimeConfig;

public class SlackWebClientProvider {
    public static SlackClient getSlackClient(String token) {
        SlackClientRuntimeConfig runtimeConfig = SlackClientRuntimeConfig.builder()
                .setTokenSupplier(() -> token)
                .build();
        return new CustomSlackWebClient(runtimeConfig);
    }
}
