package com.dojo.notifications.service.notificationService;

import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.messageGenerator.slack.SlackMessageGenerator;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.client.SlackClientManager;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SlackNotificationService implements NotificationService {

    private final SlackClientManager slackClientManager;
    private final Map<NotificationType, SlackMessageGenerator> slackMessageGenerators;


    @Autowired
    public SlackNotificationService(SlackClientManager slackClientManager, Collection<SlackMessageGenerator> slackMessageGenerators) {
        this.slackClientManager = slackClientManager;
        this.slackMessageGenerators = slackMessageGenerators.stream()
                .collect(Collectors.toMap(SlackMessageGenerator::getMessageGeneratorTypeMapping, Function.identity()));

    }

    @Override
    public NotifierType getNotificationServiceTypeMapping() {
        return NotifierType.SLACK;
    }

    // Notify user
    @Override
    public void notify(UserDetails userDetails, Notification notification, Contest contest) {
        CustomSlackClient slackClient = getSlackClient(contest);
        String slackChannel = slackClient.getConversationId(userDetails.getEmail());
        slackClient.postMessage(notification.getAsSlackNotification(slackMessageGenerators.get(notification.getType()), slackClient, slackChannel));
    }

    // Notify channel
    @Override
    public void notify(Notification notification, Contest contest) {
        CustomSlackClient slackClient = getSlackClient(contest);
        String slackChannel = contest.getSlackChannel();
        slackClient.postMessage(notification.getAsSlackNotification(slackMessageGenerators.get(notification.getType()), slackClient, slackChannel));
    }

    private CustomSlackClient getSlackClient(Contest contest) {
        return slackClientManager.getSlackClient(contest.getSlackToken());
    }
}
