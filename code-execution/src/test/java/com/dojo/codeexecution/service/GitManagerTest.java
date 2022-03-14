package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.github.GitConfigProperties;
import com.dojo.codeexecution.service.git.GitManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class GitManagerTest {

    private final String username = "dummy-user";
    private static final String TEMPLATE_URL = "https://github.com/dojoprojectrepos/docker-test-child";

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
        String expected = "https://github.com/dummy-user/gamified-hiring-dummy-user-kata";
        URL repository = new URL(expected);

        //Act
        when(dummyUser.getLogin()).thenReturn(username);
        List<GHUser> users = Collections.singletonList(dummyUser);
        when(gitHub.searchUsers().q(username).list().toList()).thenReturn(users);
        when(gitHub.getMyself().getLogin()).thenReturn("https://github.com/dummy-user");
        when(gitHub.getUser(eq(username)).getLogin()).thenReturn(username);
        when(gitHub.getRepository(expected).getHtmlUrl()).thenReturn(repository);

        String actual = gitManager.getExistingGitHubRepository(username, "kata").toString();

        //Assert
        assertEquals(expected, actual);
    }

    @Test
    public void createGitHubRepository() throws IOException {
        //Arrange
        String repositoryName = "gamified-hiring/dummy-user-kata";

        //Act
        when(gitHub.getUser(username)).thenReturn(dummyUser);
        when(dummyUser.getLogin()).thenReturn(username);
        List<GHUser> users = Collections.singletonList(dummyUser);
        when(gitHub.searchUsers().q(username).list().toList()).thenReturn(users);

        //Assert
        gitManager.createGitHubRepository(username, "kata", TEMPLATE_URL);
        verify(gitHub, times(1)).createRepository(repositoryName);
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
        when(gitHub.getRepository(any())).thenThrow(IOException.class);

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

    @Nested
    @ExtendWith(SpringExtension.class)
    class DeleteTests {

        private final String REPO_PREFIX = "gamified-hiring-";
        private final String GAME = "kata";
        private final String SUFFIX = "-" + GAME;
        private final String RANDOM_REPO_NAME = "some random name: ";

        private Map<String, GHRepository> allRepos;
        private Map<String, GHRepository> reposForDeletion;
        private Map<String, GHRepository> reposToKeep;

        @BeforeEach
        public void setUp() throws Exception{
            allRepos = new HashMap<>();
            reposForDeletion = new HashMap<>();
            reposToKeep = new HashMap<>();

            for (int i = 0; i < 100; i++) {
                GHRepository repo = mock(GHRepository.class);
                when(repo.getName()).thenReturn(REPO_PREFIX+i+SUFFIX);
                allRepos.put(repo.getName(), repo);
                reposForDeletion.put(repo.getName(), repo);
            }

            for (int i = 0; i < 100; i++) {
                GHRepository repo = mock(GHRepository.class);
                when(repo.getName()).thenReturn(RANDOM_REPO_NAME + i);
                allRepos.put(repo.getName(), repo);
                reposToKeep.put(repo.getName(), repo);
            }

            when(gitHub.getMyself()).thenReturn(myself);
            when(myself.getAllRepositories()).thenReturn(allRepos);
        }

        @Test
        public void testDeleteAllReposForSpecificGame() throws IOException {
            List<String> deletedRepos = new ArrayList<>(reposForDeletion.keySet());
            Collections.sort(deletedRepos);
            String expectedResult = "Following repositories have been deleted:"
                    + System.lineSeparator() + String.join(System.lineSeparator(), deletedRepos);
            assertEquals(expectedResult, gitManager.deleteReposForParticularGame(GAME));
        }

        @Test
        public void TestRepositoryDeletion_OnlyReposWithQualifyingNameShouldBeDeleted() throws Exception {
            gitManager.deleteReposForParticularGame(GAME);
            reposForDeletion.values().forEach(repo -> {
                try {
                    verify(repo, times(1)).delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            reposToKeep.values().forEach(repo -> {
                try {
                    verify(repo, times(0)).delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }
}
