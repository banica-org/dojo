package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.GitConfigProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitManagerTest {

    private static final String WEB_HOOK_PREFIX = "web";
    private static final String REPO_PREFIX = "gamified-hiring";

    @InjectMocks
    private GitManager gitManager;

    @Mock
    private GitConfigProperties gitConfig;

    @Mock
    private GitHub gitHub;

    @Test
    public void getRepo() throws IOException {
        GHUser ghUser = new GHUser();
        String email = "dummy-user";
        //when(gitHub.searchUsers()).thenReturn(new GHUserSearchBuilder());
        when(gitHub.searchUsers().q(email).list().toList())
                .thenReturn(Collections.singletonList(ghUser));
        when(ghUser.getLogin()).thenReturn("dummy-user");
        when(gitHub.getMyself().getLogin()).thenReturn("dummy-user");
        when(gitHub.getRepository(ghUser.getLogin()).getHtmlUrl())
                .thenReturn(new URL("https://github.com/dummy-user/gamified-hiring-dummy-user"));
    }
}
