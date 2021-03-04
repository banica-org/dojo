package com.dojo.notifications.service.emailNotifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.util.Map;

@Service
public abstract class MailContentBuilder {

    private final TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public abstract String generateMailContent(Map<String,Object> contextParams);

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}