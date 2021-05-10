package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.leaderboard.SlackNotificationUtils;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.Attachment;
import com.hubspot.slack.client.models.actions.Action;
import com.hubspot.slack.client.models.actions.ActionType;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LeaderboardSlackMessageGenerator extends SlackMessageGenerator {

    private static final String TITLE = "Leaderboard update";

    private static final String BUTTON_TEXT = "View Leaderboard in Dojorena";
    private static final String BUTTON_STYLE = "primary";
    //TODO Change this to real url and add event id
    private static final String BUTTON_REDIRECT_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";
    private static final String USER = "*User*";
    private static final String SCORE = "*Score*";

    private Text leaderboardNames = null;
    private Text leaderboardScores = null;

    private UserDetails userDetails = null;

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.LEADERBOARD;
    }

    @Override
    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Map<String, Object> contextParams, CustomSlackClient slackClient, String slackChannel) {
        if (contextParams.containsKey(USERDETAILS_KEY) && contextParams.get(USERDETAILS_KEY) instanceof UserDetails) {
            this.userDetails = (UserDetails) contextParams.get(USERDETAILS_KEY);
        }

        if (contextParams.get(CONTENT_KEY) instanceof Leaderboard) {
            Leaderboard leaderboard = (Leaderboard) contextParams.get(CONTENT_KEY);

            this.leaderboardNames = leaderboard.buildLeaderboardNames(userDetails, userDetailsService, slackClient);
            this.leaderboardScores = leaderboard.buildLeaderboardScores(userDetails);
        }

        return super.generateMessage(userDetailsService, contextParams, slackClient, slackChannel);
    }

    @Override
    protected ChatPostMessageParams.Builder getChatPostMessageParams(Object object, String slackChannel, String message) {
        ChatPostMessageParams.Builder builder = super.getChatPostMessageParams(object, slackChannel, message);
        if (leaderboardNames != null && leaderboardScores != null) {
            builder.addBlocks(Section.of(Text.of(TextType.MARKDOWN, SlackNotificationUtils.makeBold(TITLE)))
                    .withFields(
                            Text.of(TextType.MARKDOWN, USER),
                            Text.of(TextType.MARKDOWN, SCORE),
                            leaderboardNames,
                            leaderboardScores));
        }
        builder
                .addAttachments(Attachment.builder()
                        .addActions(Action.builder()
                                .setType(ActionType.BUTTON)
                                .setText(BUTTON_TEXT)
                                .setRawStyle(BUTTON_STYLE)
                                .setUrl(BUTTON_REDIRECT_URL)
                                .build())
                        .build());
        return builder;
    }
}
