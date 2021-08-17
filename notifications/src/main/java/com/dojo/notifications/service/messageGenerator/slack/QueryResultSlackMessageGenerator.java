package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.leaderboard.SlackNotificationUtils;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.FlinkTableService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableColumn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryResultSlackMessageGenerator extends SlackMessageGenerator {

    private static final String RESULT = "Query result: ";

    private final FlinkTableService flinkTableService;

    public QueryResultSlackMessageGenerator(FlinkTableService flinkTableService) {
        this.flinkTableService = flinkTableService;
    }

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.QUERY_RESULT;
    }

    @Override
    protected ChatPostMessageParams.Builder getChatPostMessageParams(Object object, String slackChannel, String message) {
        ChatPostMessageParams.Builder builder = super.getChatPostMessageParams(object, slackChannel, message);

        Table table = (Table) object;
        List<TableColumn> tableColumns = flinkTableService.getTableColumns(table);
        List<List<String>> rows = flinkTableService.getTableRows(table);

        for (List<String> row : rows) {
            builder.addBlocks(Section.of(Text.of(TextType.MARKDOWN, SlackNotificationUtils.makeBold(RESULT)))
                    .withFields(
                            flinkTableService.buildColumnNames(tableColumns),
                            flinkTableService.buildFields(row)))
                    .addBlocks(Divider.builder().build());
        }
        return builder;
    }
}
