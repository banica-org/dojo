package com.dojo.notifications.model.client;

import com.hubspot.slack.client.SlackClientRuntimeConfig;
import org.springframework.stereotype.Component;

@Component
public class SlackClientManager {

    public CustomSlackClient getSlackClient(String token) {
        SlackClientRuntimeConfig runtimeConfig = SlackClientRuntimeConfig.builder()
                .setTokenSupplier(() -> token)
                .build();
        return new CustomSlackWebClient(runtimeConfig);
    }
}
