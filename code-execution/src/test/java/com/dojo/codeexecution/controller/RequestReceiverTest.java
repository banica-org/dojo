package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.GitManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class RequestReceiverTest {

    @InjectMocks
    private RequestReceiver requestReceiver;

    @Mock
    private GitManager gitManager;

    @Test
    public void getRepoReturnsRepoName(){
        //Arrange
        String expected = "https://github.com/account/gamified-hiring-dummy-account";
        String username = "dummy-account";

        //Act
        when(gitManager.getRepository(username)).thenReturn(expected);
        String actual = requestReceiver.getRepo(username);

        //Assert
        assertEquals(expected, actual);
        verify(gitManager, times(1)).getRepository(username);
    }

}