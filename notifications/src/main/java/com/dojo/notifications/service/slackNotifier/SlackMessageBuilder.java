package com.dojo.notifications.service.slackNotifier;

import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.objects.Text;
import org.springframework.stereotype.Service;

@Service
public abstract class SlackMessageBuilder {

    public abstract ChatPostMessageParams.Builder generateSlackContent(String title, Text leaderboardNames, Text leaderboardScores);
}
