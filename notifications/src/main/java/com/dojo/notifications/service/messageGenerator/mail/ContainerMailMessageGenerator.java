package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;

@Service
public class ContainerMailMessageGenerator extends MailMessageGenerator {
    private static final String TEMPLATE = "containerMailTemplate";

    public ContainerMailMessageGenerator(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.CONTAINER;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
