package com.dojo.notifications.model.notification.leaderboard;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import com.dojo.notifications.service.messageGenerator.slack.SlackMessageGenerator;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

public class CommonLeaderboardNotification extends LeaderboardNotification {

    public CommonLeaderboardNotification(UserDetailsService userDetailsService, Leaderboard leaderboard, String message) {
        super(userDetailsService, leaderboard, message);
    }

    @Override
    public ChatPostMessageParams getAsSlackNotification(SlackMessageGenerator slackMessageGenerator, CustomSlackClient slackClient, String slackChannel) {
        return slackMessageGenerator.generateMessage(userDetailsService, leaderboard, slackClient, slackChannel, super.message);
    }

    @Override
    public String getAsEmailNotification(MailMessageGenerator mailMessageGenerator) {
        return mailMessageGenerator.generateMessage(super.getContextParams());
    }
}
