package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.FlinkTableService;
import org.apache.flink.table.api.Table;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class QueryResultMailMessageGenerator extends MailMessageGenerator {
    private static final String MESSAGE = "message";
    private static final String CONTENT = "content";
    private static final String COLUMN_NAMES = "columns";
    private static final String ROWS = "rows";
    private static final String TEMPLATE = "queryResultMailTemplate";

    private final FlinkTableService flinkTableService;

    public QueryResultMailMessageGenerator(ITemplateEngine templateEngine, FlinkTableService flinkTableService) {
        super(templateEngine);
        this.flinkTableService = flinkTableService;
    }

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.QUERY_RESULT;
    }

    @Override
    public String generateMessage(Map<String, Object> contextParams) {
        Context context = new Context();
        context.setVariable(MESSAGE, contextParams.get(MESSAGE));

        Table table = (Table) contextParams.get(CONTENT);
        context.setVariable(COLUMN_NAMES, flinkTableService.getTableColumns(table));
        context.setVariable(ROWS, flinkTableService.getTableRows(table));

        return getTemplateEngine().process(getTemplate(), context);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
