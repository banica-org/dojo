package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import com.vdurmont.emoji.EmojiParser;
import org.markdown4j.ExtDecorator;
import org.markdown4j.Markdown4jProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Map;

@Service
public abstract class MailMessageGenerator {

    private final ITemplateEngine templateEngine;

    @Autowired
    public MailMessageGenerator(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public abstract NotificationType getMessageGeneratorTypeMapping();

    public abstract String getTemplate();

    public String generateMessage(Map<String, Object> contextParams) {
        Context context = new Context();
        contextParams.forEach(context::setVariable);

        //MarkDown to HTML
        String message = convertToHTML(context);
        String emojiConvert = EmojiParser.parseToHtmlDecimal((message));

        return getTemplateEngine().process(getTemplate(), context).replace("%s", emojiConvert);
    }

    public ITemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    private String convertToHTML(Context context) {
        try {
            String message = (String) context.getVariable("message");
            message = message.replace("*", "**");
            return new Markdown4jProcessor().process(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "There was an error";
    }
}