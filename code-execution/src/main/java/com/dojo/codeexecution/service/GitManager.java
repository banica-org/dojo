package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.GitConfigProperties;
import com.dojo.codeexecution.controller.RequestReceiver;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Service
public class GitManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestReceiver.class);

    private static final String WEB_HOOK_PREFIX = "web";
    private static final String REPO_PREFIX = "gamified-hiring";

    private final GitConfigProperties gitConfig;
    private final GitHub gitHub;

    @Autowired
    public GitManager(GitConfigProperties gitConfig, GitHub gitHub) {
        this.gitConfig = gitConfig;
        this.gitHub = gitHub;
    }

    @Retryable
    public URL getExistingGitHubRepository(String username) throws IOException {
        return gitHub.getRepository(getRepositoryNameByOwner(username)).getHtmlUrl();
    }

    @Retryable
    public URL createGitHubRepository(String username) throws IOException {
        GHUser user = getGitHubUser(username);
        String repoName = REPO_PREFIX + "/" + user.getLogin();

        GHRepository repo = gitHub.createRepository(repoName).private_(true).create();

        repo.createHook(WEB_HOOK_PREFIX, gitConfig.getWebhookConfig(),
                Collections.singletonList(GHEvent.PUSH), true);
        repo.addCollaborators(user);

        return repo.getHtmlUrl();
    }

    public boolean hasUserExistingRepository(String username) {
        try {
            gitHub.getRepository(getRepositoryNameByOwner(username));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private String getRepositoryNameByOwner(String username) throws IOException {
        return gitHub.getMyself().getLogin() + "/" + REPO_PREFIX + "-" + getGitHubUser(username).getLogin();
    }

    private GHUser getGitHubUser(String username) throws IOException {
        List<GHUser> users = gitHub.searchUsers().q(username).list().toList();
        if (users.size() == 1) {
            return users.get(0);
        }
        throw new IllegalArgumentException("User not found!");
    }

}
