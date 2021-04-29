package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;

@Service
public class ImageMailMessageGenerator extends MailMessageGenerator {
    private static final String TEMPLATE = "imageMailTemplate";

    public ImageMailMessageGenerator(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.IMAGE;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
