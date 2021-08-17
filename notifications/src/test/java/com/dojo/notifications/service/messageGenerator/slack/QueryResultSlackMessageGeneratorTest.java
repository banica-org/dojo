package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.FlinkTableService;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Block;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableColumn;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class QueryResultSlackMessageGeneratorTest {

    private static final String TABLE_COLUMN = "column";
    private static final String TABLE_FIELD = "field";
    private static final String CONTENT_KEY = "content";
    private static final String MESSAGE_KEY = "message";
    private static final String MESSAGE = "Query result: ";
    private static final String CHANNEL = "Channel";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;

    @Mock
    private TableColumn tableColumn;

    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private CustomSlackClient slackClient;

    @Mock
    private FlinkTableService flinkTableService;

    private QueryResultSlackMessageGenerator queryResultSlackMessageGenerator;

    @Before
    public void init() {
        queryResultSlackMessageGenerator = new QueryResultSlackMessageGenerator(flinkTableService);
    }

    @Test
    public void getMessageGeneratorTypeMappingTest() {
        NotificationType expected = NotificationType.QUERY_RESULT;
        NotificationType actual = queryResultSlackMessageGenerator.getMessageGeneratorTypeMapping();
        assertEquals(expected, actual);
    }

    @Test
    public void generateMessageTest() {
        when(tableColumn.getName()).thenReturn(TABLE_COLUMN);
        when(flinkTableService.getTableColumns(table)).thenReturn(Collections.singletonList(tableColumn));
        when(flinkTableService.getTableRows(table)).thenReturn(Collections.singletonList(Collections.singletonList(TABLE_FIELD)));
        when(flinkTableService.buildColumnNames(Collections.singletonList(tableColumn))).thenReturn(Text.of(TextType.MARKDOWN, TABLE_COLUMN));
        when(flinkTableService.buildFields(Collections.singletonList(TABLE_FIELD))).thenReturn(Text.of(TextType.MARKDOWN, TABLE_FIELD));

        ChatPostMessageParams content = queryResultSlackMessageGenerator.generateMessage(userDetailsService, getContextParams(), slackClient, CHANNEL);
        List<Block> blocks = content.getBlocks();

        assertEquals(4, blocks.size());
        assertTrue(blocks.get(2).toString().contains(MESSAGE));
        assertTrue(blocks.get(2).toString().contains(TABLE_COLUMN));
        assertTrue(blocks.get(2).toString().contains(TABLE_FIELD));
    }

    private Map<String, Object> getContextParams() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(MESSAGE_KEY, MESSAGE);
        contextParams.put(CONTENT_KEY, table);
        return contextParams;
    }
}
