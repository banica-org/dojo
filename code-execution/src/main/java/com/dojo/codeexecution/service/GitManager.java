package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.GitConfigProperties;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Service
public class GitManager {

    private static final String WEB_HOOK_PREFIX = "web";
    private static final String REPO_PREFIX = "gamified-hiring";

    @Autowired
    private GitConfigProperties gitConfig;

    private GitHub gitHub;

    @Retryable
    public URL getRepo(String email) throws IOException {
        URL repoUrl;
        GHUser ghUser = getGhUser(email);
        String login = ghUser.getLogin();

        try {
            repoUrl = gitHub.getRepository(getRepoNameWithOwner(login)).getHtmlUrl();
        } catch (IOException e) {
            repoUrl = createGitRepo(gitHub, login, ghUser);
        }

        return repoUrl;
    }

    private String getRepoNameWithOwner(String login) throws IOException {
        return gitHub.getMyself().getLogin() + "/" + REPO_PREFIX + "-" + login;
    }

    private GHUser getGhUser(String email) throws IOException {
        List<GHUser> users = gitHub.searchUsers().q(email).list().toList();
        if (users.size() == 1) {
            return users.get(0);
        }
        throw new IllegalArgumentException("User email not visible!");
    }

    private URL createGitRepo(GitHub github, String login, GHUser user) throws IOException {
        String repoName = REPO_PREFIX + "/" + login;
        GHRepository repo = github.createRepository(repoName).private_(true).create();

        repo.createHook(WEB_HOOK_PREFIX, gitConfig.getWebhookConfig(),
                Collections.singletonList(GHEvent.PUSH), true);

        repo.addCollaborators(user);

        return repo.getHtmlUrl();
    }

    @PostConstruct
    private void createGitHubInstance() {
        if (gitHub == null) {
            try {
                gitHub = new GitHubBuilder().withOAuthToken(gitConfig.getRepoToken(), gitConfig.getUser()).build();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
