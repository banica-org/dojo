package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.github.GitConfigProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class GitManagerTest {

    private final String username = "dummy-user";

    @InjectMocks
    private GitManager gitManager;

    @Mock
    private GitConfigProperties gitConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GitHub gitHub;

    @Mock
    private GHUser dummyUser;

    @Mock
    private GHRepository ghRepository;

    @Mock
    private GHHook ghHook;

    @Test
    public void getExistingRepositoryReturnsRepository() throws IOException {
        //Arrange
        String expected = "https://github.com/dummy-user/gamified-hiring-dummy-user";
        URL repository = new URL(expected);

        //Act
        when(dummyUser.getLogin()).thenReturn(username);
        List<GHUser> users = Collections.singletonList(dummyUser);
        when(gitHub.searchUsers().q(username).list().toList()).thenReturn(users);
        when(gitHub.getMyself().getLogin()).thenReturn("https://github.com/dummy-user");
        when(gitHub.getRepository(expected).getHtmlUrl()).thenReturn(repository);

        String actual = gitManager.getExistingGitHubRepository(username).toString();

        //Assert
        assertEquals(expected, actual);
    }


    @Test(expected = IllegalArgumentException.class)
    public void getExistingRepositoryReturnsNotFound() throws IOException {
        gitManager.getExistingGitHubRepository(username);
    }


    @Test
    public void createGitHubRepository() throws IOException {
        //Arrange
        String repositoryName = "gamified-hiring/dummy-user";

        //Act
        when(dummyUser.getLogin()).thenReturn(username);
        List<GHUser> users = Collections.singletonList(dummyUser);
        when(gitHub.searchUsers().q(username).list().toList()).thenReturn(users);

        //Assert
        gitManager.createGitHubRepository(username);
        verify(gitHub, times(1)).createRepository(repositoryName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryReturnsNotFound() throws IOException {
        gitManager.createGitHubRepository(username);
    }

    @Test
    public void hasUserExistingRepositoryReturnsTrue() throws IOException {
        //Act
        List<GHUser> users = Collections.singletonList(dummyUser);
        when(gitHub.searchUsers().q(username).list().toList()).thenReturn(users);
        Boolean actual = gitManager.hasUserExistingRepository(username);

        //Assert
        assertEquals(true, actual);
    }

    @Test
    public void hasUserExistingRepositoryReturnsFalse() throws IOException {
        //Act
        when(gitHub.searchUsers().q(username).list().toList()).thenThrow(IOException.class);

        Boolean actual = gitManager.hasUserExistingRepository(username);
        //Assert
        assertEquals(false, actual);
    }

    @Test
    public void buildParentWebHookWhenNoHook() throws IOException {
        //Arrange
        when(gitHub.getRepository(username)).thenReturn(ghRepository);
        when(gitConfig.getUser()).thenReturn(username);
        when(gitConfig.getParentRepositoryName()).thenReturn(username);

        //Act
        gitManager.buildParentWebHook();

        //Assert
        verify(gitHub, times(1)).getRepository(username + "/" + username);
        verify(gitConfig, times(1)).getWebhookConfig();
        verify(gitConfig, times(1)).getWebhookAddress();
        verify(gitConfig, times(1)).getUser();
        verify(gitConfig, times(1)).getParentRepositoryName();
    }

    @Test
    public void buildParentWebHookWhenIsHookAvailable() throws IOException {
        //Arrange
        when(ghRepository.getHooks()).thenReturn(Collections.singletonList(ghHook));

        when(gitHub.getRepository(username)).thenReturn(ghRepository);
        when(gitConfig.getParentRepositoryName()).thenReturn(username);
        when(gitConfig.getUser()).thenReturn(username);

        //Act
        gitManager.buildParentWebHook();

        //Assert
        verify(gitHub, times(1)).getRepository(username + "/" + username);
        verify(gitConfig, times(1)).getWebhookConfig();
        verify(gitConfig, times(1)).getWebhookAddress();
        verify(gitConfig, times(1)).getUser();
        verify(gitConfig, times(1)).getParentRepositoryName();
    }
}
