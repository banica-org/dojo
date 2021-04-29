package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;

@Service
public class TestResultsMailMessageGenerator extends MailMessageGenerator {
    private static final String TEMPLATE = "testResultsMailTemplate";

    public TestResultsMailMessageGenerator(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.TEST_RESULTS;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
