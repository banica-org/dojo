package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.FlinkTableService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class QueryResultMailMessageGeneratorTest {

    private final String EXPECTED_PROCESS_RETURN_TYPE = "PROCESSED";

    @Mock
    private ITemplateEngine iTemplateEngine;
    @Mock
    private FlinkTableService flinkTableService;

    private QueryResultMailMessageGenerator queryResultMailMessageGenerator;

    @Before
    public void init() {
        queryResultMailMessageGenerator = new QueryResultMailMessageGenerator(iTemplateEngine, flinkTableService);
    }

    @Test
    public void getMessageGeneratorTypeMapping() {
        NotificationType expected = NotificationType.QUERY_RESULT;
        NotificationType actual = queryResultMailMessageGenerator.getMessageGeneratorTypeMapping();

        assertEquals(expected, actual);
    }

    @Test
    public void generateMessageTest() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(EXPECTED_PROCESS_RETURN_TYPE, 20);
        when(iTemplateEngine.process(eq("queryResultMailTemplate"), any(Context.class))).thenReturn(EXPECTED_PROCESS_RETURN_TYPE);

        String actual = queryResultMailMessageGenerator.generateMessage(contextParams);

        Assert.assertEquals(EXPECTED_PROCESS_RETURN_TYPE, actual);
        verify(iTemplateEngine, times(1)).process(eq("queryResultMailTemplate"), any(Context.class));

    }
}
