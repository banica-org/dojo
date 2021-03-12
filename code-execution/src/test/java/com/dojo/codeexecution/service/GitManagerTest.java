package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.GitConfigProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class GitManagerTest {

    @InjectMocks
    private GitManager gitManager;

    @Mock
    private GitConfigProperties gitConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GitHub gitHub;

    @Mock
    private GHUser dummyUser;

    @Test
    public void getRepositoryReturnsRepository() throws IOException {
        //Arrange
        String expected = "https://github.com/dummy-user/gamified-hiring-dummy-user";
        String username = "dummy-user";
        URL repository = new URL(expected);

        //Act
        when(dummyUser.getLogin()).thenReturn(username);
        List<GHUser> users = Collections.singletonList(dummyUser);
        when(gitHub.searchUsers().q(username).list().toList()).thenReturn(users);
        when(gitHub.getMyself().getLogin()).thenReturn("https://github.com/dummy-user");
        when(gitHub.getRepository(expected).getHtmlUrl()).thenReturn(repository);

        String actual = gitManager.getRepository(username);

        //Assert
        assertEquals(expected, actual);
    }


    @Test
    public void getRepositoryReturnsNotFound() {
        //Arrange
        String expected = "User not found!";
        String username = "dummy-user";

        //Act
        String actual = gitManager.getRepository(username);

        //Assert
        assertEquals(expected,actual);
    }
}
