package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;

@Service
public class LeaderboardMailMessageGenerator extends MailMessageGenerator {
    private static final String TEMPLATE = "leaderboardMailTemplate";

    public LeaderboardMailMessageGenerator(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.LEADERBOARD;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
