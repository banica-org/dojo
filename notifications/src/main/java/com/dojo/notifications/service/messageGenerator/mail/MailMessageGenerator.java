package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;

import java.util.Map;

@Service
public abstract class MailMessageGenerator {

    private final ITemplateEngine templateEngine;

    @Autowired
    public MailMessageGenerator(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public abstract NotificationType getMessageGeneratorTypeMapping();

    public abstract String generateMessage(Map<String, Object> contextParams);

    public ITemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}