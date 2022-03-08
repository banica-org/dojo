package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.GitManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class RequestReceiverTest {

    private static final String TEMPLATE_URL = "https://github.com/dojoprojectrepos/docker-test-child";

    @InjectMocks
    private RequestReceiver requestReceiver;

    @Mock
    private GitManager gitManager;

    @Test
    public void getRepositoryReturnsRepository() throws IOException {
        //Arrange
        URL expectedURL = new URL("https://github.com/account/gamified-hiring-dummy-account");
        String username = "dummy-account";
        String expected = expectedURL.toString();

        //Act
        when(gitManager.hasUserExistingRepository(username,"kata")).thenReturn(true);
        when(gitManager.getExistingGitHubRepository(username, "kata")).thenReturn(expectedURL);
        String actual = requestReceiver.getRepository(username,"kata", TEMPLATE_URL);

        //Assert
        assertEquals(expected, actual);
        verify(gitManager, times(1)).getExistingGitHubRepository(username, "kata");
        verify(gitManager, times(1)).hasUserExistingRepository(username, "kata");
    }

    @Test
    public void createRepositoryReturnsRepository() throws IOException {
        //Arrange
        URL expectedURL = new URL("https://github.com/account/gamified-hiring-dummy-account");
        String username = "dummy-account";
        String expected = expectedURL.toString();

        //Act
        when(gitManager.hasUserExistingRepository(username, "kata")).thenReturn(false);
        when(gitManager.createGitHubRepository(username, "kata", TEMPLATE_URL)).thenReturn(expectedURL);
        String actual = requestReceiver.getRepository(username,"kata", TEMPLATE_URL);

        //Assert
        assertEquals(expected, actual);
        verify(gitManager, times(1)).hasUserExistingRepository(username, "kata");
        verify(gitManager, times(1)).createGitHubRepository(username, "kata", TEMPLATE_URL);
    }
}