package com.dojo.notifications.client;

import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import org.springframework.stereotype.Component;

@Component
public class SlackWebClientProvider {

    public SlackClient getSlackClient(String token) {
        SlackClientRuntimeConfig runtimeConfig = SlackClientRuntimeConfig.builder()
                .setTokenSupplier(() -> token)
                .build();
        return new CustomSlackWebClient(runtimeConfig);
    }
}
