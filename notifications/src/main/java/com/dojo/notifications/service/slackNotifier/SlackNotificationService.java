package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.client.SlackWebClientProvider;
import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.enums.NotifierType;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.NotificationService;
import com.hubspot.algebra.Result;
import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.methods.params.conversations.ConversationOpenParams;
import com.hubspot.slack.client.methods.params.users.UserEmailParams;
import com.hubspot.slack.client.models.conversations.Conversation;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import com.hubspot.slack.client.models.response.conversations.ConversationsOpenResponse;
import com.hubspot.slack.client.models.response.users.UsersInfoResponse;
import com.hubspot.slack.client.models.users.SlackUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlackNotificationService implements NotificationService {

    private final SlackWebClientProvider slackWebClientProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackNotificationService.class);

    @Autowired
    public SlackNotificationService(SlackWebClientProvider slackWebClientProvider) {
        this.slackWebClientProvider = slackWebClientProvider;
    }

    @Override
    public NotifierType getNotificationServiceTypeMapping() {
        return NotifierType.SLACK;
    }

    // Notify user
    @Override
    public void notify(UserDetails userDetails, Notification notification, Contest contest) {
        SlackClient slackClient = slackWebClientProvider.getSlackClient(contest.getSlackToken());
        String slackChannel = this.getSlackChannel(userDetails.getEmail(), slackClient);
        this.notify(notification, slackClient, slackChannel);
    }

    // Notify channel
    @Override
    public void notify(Notification notification, Contest contest) {
        SlackClient slackClient = slackWebClientProvider.getSlackClient(contest.getSlackToken());
        String slackChannel = contest.getSlackChannel();
        this.notify(notification, slackClient, slackChannel);

    }

    private void notify(Notification notification, SlackClient slackClient, String slackChannel) {
        ChatPostMessageParams chatPostMessageParams = buildChatPostMessageParams(notification, slackClient, slackChannel);
        postMessage(chatPostMessageParams, slackClient);
    }

    private ChatPostMessageParams buildChatPostMessageParams(Notification notification, SlackClient slackClient, String slackChannel) {
        return notification
                .convertToSlackNotification(this::getSlackUserId, slackClient)
                .setChannelId(slackChannel)
                .build();
    }

    private void postMessage(ChatPostMessageParams chatPostMessageParams, SlackClient slackClient) {
        Result<ChatPostMessageResponse, SlackError> postResult = slackClient.postMessage(chatPostMessageParams).join();
        try {
            postResult.unwrapOrElseThrow(); // release failure here as a RTE
        } catch (IllegalStateException e) {
            LOGGER.warn("Error occurred while trying to send Slack notification to channel {}.", chatPostMessageParams.getChannelId());
            return;
        }
        LOGGER.info("Slack notification send to channel {}.", chatPostMessageParams.getChannelId());
    }

    private String getSlackChannel(String email, SlackClient slackClient) {
        try {
            ConversationOpenParams conversationOpenParams = ConversationOpenParams.builder()
                    .addUsers(getSlackUserId(email, slackClient))
                    .build();
            ConversationsOpenResponse response = slackClient.openConversation(conversationOpenParams)
                    .join().unwrapOrElseThrow();
            Conversation conversation = response.getConversation();
            return conversation.getId();
        } catch (IllegalStateException e) {
            LOGGER.warn("Could not find conversation for user with email {}.", email);
            return "";
        }
    }

    private String getSlackUserId(String email, SlackClient slackClient) {
        try {
            return getUser(email, slackClient).getId();
        } catch (IllegalStateException e) {
            LOGGER.warn("Could not find user with email {}.", email);
            return "";
        }
    }

    private SlackUser getUser(String email, SlackClient slackClient) {
        UserEmailParams userEmailParams = UserEmailParams.builder()
                .setEmail(email)
                .build();
        UsersInfoResponse response = slackClient
                .lookupUserByEmail(userEmailParams)
                .join().unwrapOrElseThrow();
        return response.getUser();
    }
}
