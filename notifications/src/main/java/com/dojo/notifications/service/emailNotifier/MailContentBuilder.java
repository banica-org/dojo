package com.dojo.notifications.service.emailNotifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;

import java.util.Map;

@Service
public abstract class MailContentBuilder {

    private final ITemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public abstract String generateMailContent(Map<String,Object> contextParams);

    public ITemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}