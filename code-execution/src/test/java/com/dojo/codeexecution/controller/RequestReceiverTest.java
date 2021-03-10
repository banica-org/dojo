package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.GitManager;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class RequestReceiverTest {

    @InjectMocks
    private RequestReceiver requestReceiver;

    @Mock
    private GitManager gitManager;

    @Test
    public void getRepo() throws IOException {
        //Arrange
        String expected = "https://github.com/account/gamified-hiring-dummy-account";
        String email = "dummy-account";

        //Act
        when(gitManager.getRepo(email))
                .thenReturn(new URL("https://github.com/account/gamified-hiring-dummy-account"));
        String actual = requestReceiver.getRepo(email);

        //Assert
        assertEquals(expected, actual);
        verify(gitManager, times(1)).getRepo(email);
    }
}