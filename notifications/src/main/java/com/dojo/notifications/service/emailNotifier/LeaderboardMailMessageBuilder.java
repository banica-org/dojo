package com.dojo.notifications.service.emailNotifier;

import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
public class LeaderboardMailMessageBuilder extends MailContentBuilder {

    public LeaderboardMailMessageBuilder(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public String generateMailContent(Map<String, Object> contextParams) {
        Context context = new Context();
        contextParams.forEach(context::setVariable);

        return getTemplateEngine().process("mailTemplate", context);
    }
}
