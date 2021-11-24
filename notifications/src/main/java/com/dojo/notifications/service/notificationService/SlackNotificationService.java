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

    //Expires 24 days after 26/10/2021
    private static final String INVITATION_URL = "https://join.slack.com/t/dojo-dpa3499/shared_invite/zt-xbluulpj-bBihigTmj6x3WlSkuiCeUA";

    private final SlackClientManager slackClientManager;
    private final Map<NotificationType, SlackMessageGenerator> slackMessageGenerators;
    private final EmailNotificationService emailNotificationService;


    @Autowired
    public SlackNotificationService(SlackClientManager slackClientManager, Collection<SlackMessageGenerator> slackMessageGenerators, EmailNotificationService emailNotificationService) {
        this.slackClientManager = slackClientManager;
        this.slackMessageGenerators = slackMessageGenerators.stream()
                .collect(Collectors.toMap(SlackMessageGenerator::getMessageGeneratorTypeMapping, Function.identity()));

        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public NotifierType getNotificationServiceTypeMapping() {
        return NotifierType.SLACK;
    }

    // Notify user
    @Override
    public void notify(UserDetails userDetails, Notification notification, Contest contest) {
        if (!userDetails.getSlackEmail().equals("") && userDetails.isSlackSubscription()) {
            CustomSlackClient slackClient = getSlackClient(contest);
            if (slackClient.getConversationId(userDetails.getSlackEmail()).equals("")) {
                emailNotificationService.notifyForSlackInvitation(userDetails.getEmail(), INVITATION_URL, contest);
            } else {
                String slackChannel = slackClient.getConversationId(userDetails.getSlackEmail());
                slackClient.postMessage(notification.getAsSlackNotification(slackMessageGenerators.get(notification.getType()), slackClient, slackChannel));
            }
        }
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
