package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.github.GitConfigProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHMyself;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
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

    @Mock
    private GHMyself myself;

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

        String actual = gitManager.getExistingGitHubRepository(username, "kata").toString();

        //Assert
        assertEquals(expected, actual);
    }


    @Test(expected = IllegalArgumentException.class)
    public void getExistingRepositoryReturnsNotFound() throws IOException {
        gitManager.getExistingGitHubRepository(username, "kata");
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
        gitManager.createGitHubRepository(username, "kata");
        verify(gitHub, times(1)).createRepository(repositoryName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryReturnsNotFound() throws IOException {
        gitManager.createGitHubRepository(username, "kata");
    }

    @Test
    public void hasUserExistingRepositoryReturnsTrue() throws IOException {
        //Act
        List<GHUser> users = Collections.singletonList(dummyUser);
        when(gitHub.searchUsers().q(username).list().toList()).thenReturn(users);
        Boolean actual = gitManager.hasUserExistingRepository(username, "kata");

        //Assert
        assertEquals(true, actual);
    }

    @Test
    public void hasUserExistingRepositoryReturnsFalse() throws IOException {
        //Act
        when(gitHub.searchUsers().q(username).list().toList()).thenThrow(IOException.class);

        Boolean actual = gitManager.hasUserExistingRepository(username, "kata");
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

    @Test
    public void testDeleteAllReposForSpecificGame() throws IOException{
        when(ghRepository.getName()).thenReturn("gamified-hiring-user1-kata");
        GHRepository repo2 = mock(GHRepository.class);
        when(repo2.getName()).thenReturn("gamified-hiring-user2-kata");
        GHRepository repo3 = mock(GHRepository.class);
        when(repo3.getName()).thenReturn("gamified-hiring-unqualifying-bomberman");
        Map<String, GHRepository> repoMap = new HashMap<>();
        repoMap.put(ghRepository.getName(), ghRepository);
        repoMap.put(repo2.getName(), repo2);
        repoMap.put(repo3.getName(), repo3);
        when(gitHub.getMyself()).thenReturn(myself);
        when(myself.getAllRepositories()).thenReturn(repoMap);
        String expectedResult = "Following repositories have been deleted: " + System.lineSeparator() +
                "gamified-hiring-user2-kata" + System.lineSeparator()+
                "gamified-hiring-user1-kata";
        assertEquals(expectedResult, gitManager.deleteReposForParticularGame("kata"));
    }

    @Test
    public void TestRepositoryDeletion_OnlyReposWithQualifyingNameShouldBeDeleted() throws Exception{
        Map<String, GHRepository> allRepos = new HashMap<>();
        Map<String, GHRepository> reposForDeletion = new HashMap<>();
        Map<String, GHRepository> reposToKeep = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            GHRepository repo = mock(GHRepository.class);
            when(repo.getName()).thenReturn("gamified-hiring-"+i+"-kata");
            allRepos.put(repo.getName(), repo);
            reposForDeletion.put(repo.getName(), repo);
        }

        for (int i = 0; i < 100; i++) {
            GHRepository repo = mock(GHRepository.class);
            when(repo.getName()).thenReturn("some random name: " + i);
            allRepos.put(repo.getName(), repo);
            reposToKeep.put(repo.getName(), repo);
        }

        when(gitHub.getMyself()).thenReturn(myself);
        when(myself.getAllRepositories()).thenReturn(allRepos);
        gitManager.deleteReposForParticularGame("kata");
        reposForDeletion.values().forEach(repo-> {
            try {
                verify(repo,times(1)).delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        reposToKeep.values().forEach(repo-> {
            try {
                verify(repo,times(0)).delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
