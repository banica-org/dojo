package com.dojo.notifications.model.notification.leaderboard;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import com.dojo.notifications.service.messageGenerator.slack.SlackMessageGenerator;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

import java.util.Map;

public class PersonalLeaderboardNotification extends LeaderboardNotification {

    private final UserDetails userDetails;

    public PersonalLeaderboardNotification(UserDetailsService userDetailsService, Leaderboard leaderboard, UserDetails userDetails, String message) {
        super(userDetailsService, leaderboard, message);
        this.userDetails = userDetails;
    }

    @Override
    public ChatPostMessageParams getAsSlackNotification(SlackMessageGenerator slackMessageGenerator, CustomSlackClient slackClient, String slackChannel) {
        return slackMessageGenerator.generateMessage(userDetailsService, userDetails, leaderboard, slackClient, slackChannel, super.message);
    }

    @Override
    public String getAsEmailNotification(MailMessageGenerator mailMessageGenerator) {
        Map<String, Object> contextParams = super.getContextParams();
        contextParams.put(USERDETAILS_KEY, userDetails);
        return mailMessageGenerator.generateMessage(contextParams);
    }


}
