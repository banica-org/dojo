package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class SlackMessageBuilder {

    public abstract ChatPostMessageParams generateSlackContent(UserDetails userDetails, List<User> leaderboard, CustomSlackClient slackClient, String slackChannel);
    public abstract ChatPostMessageParams generateSlackContent(List<User> leaderboard, CustomSlackClient slackClient, String slackChannel);
}
