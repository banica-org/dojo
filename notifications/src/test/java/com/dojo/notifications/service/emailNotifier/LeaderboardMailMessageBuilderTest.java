package com.dojo.notifications.service.emailNotifier;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.ITemplateEngine;

import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardMailMessageBuilderTest {

    private final String EXPECTED_PROCESS_RETURN_TYPE = "PROCESSED";

    @Mock
    private ITemplateEngine templateEngine;

    @InjectMocks
    private LeaderboardMailMessageBuilder leaderboardMailMessageBuilder;

    @Test
    public void generateMailContentTest() {
        //Arrange
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(EXPECTED_PROCESS_RETURN_TYPE, 20);
        contextParams.put(EXPECTED_PROCESS_RETURN_TYPE, 14);

        Context context = new Context();
        contextParams.forEach(context::setVariable);

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(EXPECTED_PROCESS_RETURN_TYPE);

        //Act
        String actual = leaderboardMailMessageBuilder.generateMailContent(contextParams);

        //Assert
        Assert.assertEquals(EXPECTED_PROCESS_RETURN_TYPE, actual);
        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
    }
}
