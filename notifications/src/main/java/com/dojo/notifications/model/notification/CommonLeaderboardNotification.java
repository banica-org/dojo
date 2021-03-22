package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.emailNotifier.MailContentBuilder;
import com.dojo.notifications.service.slackNotifier.SlackMessageBuilder;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

public class CommonLeaderboardNotification extends LeaderboardNotification {

    public CommonLeaderboardNotification(UserDetailsService userDetailsService, Leaderboard leaderboard) {
        super(userDetailsService, leaderboard);
    }

    @Override
    public ChatPostMessageParams getAsSlackNotification(SlackMessageBuilder slackMessageBuilder, CustomSlackClient slackClient, String slackChannel) {
        return slackMessageBuilder.generateSlackContent(userDetailsService, leaderboard, slackClient, slackChannel);
    }

    @Override
    public String getAsEmailNotification(MailContentBuilder mailContentBuilder) {
        return mailContentBuilder.generateMailContent(super.getContextParams());
    }
}
