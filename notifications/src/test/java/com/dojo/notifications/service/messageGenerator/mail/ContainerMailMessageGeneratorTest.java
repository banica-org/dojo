package com.dojo.notifications.service.messageGenerator.mail;

import com.dojo.notifications.model.notification.enums.NotificationType;
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
public class ContainerMailMessageGeneratorTest {

    private final String EXPECTED_PROCESS_RETURN_TYPE = "PROCESSED";
    private final String MESSAGE = "message";

    @Mock
    private ITemplateEngine iTemplateEngine;

    private ContainerMailMessageGenerator containerMailMessageGenerator;

    @Before
    public void init() {
        containerMailMessageGenerator = new ContainerMailMessageGenerator(iTemplateEngine);
    }

    @Test
    public void getMessageGeneratorTypeMapping() {
        NotificationType expected = NotificationType.CONTAINER;
        NotificationType actual = containerMailMessageGenerator.getMessageGeneratorTypeMapping();

        assertEquals(expected, actual);
    }

    @Test
    public void generateMessageTest() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(EXPECTED_PROCESS_RETURN_TYPE, 20);
        contextParams.put(MESSAGE, "html");
        when(iTemplateEngine.process(eq("containerMailTemplate"), any(Context.class))).thenReturn(EXPECTED_PROCESS_RETURN_TYPE);

        String actual = containerMailMessageGenerator.generateMessage(contextParams);

        Assert.assertEquals(EXPECTED_PROCESS_RETURN_TYPE, actual);
        verify(iTemplateEngine, times(1)).process(eq("containerMailTemplate"), any(Context.class));

    }
}
