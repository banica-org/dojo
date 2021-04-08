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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GitManager {

    public static final String HOOK_URL = "http://192.168.1.104:8081/pushEvent";
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

    @PostConstruct
    public void buildParentWebHook() throws IOException {
        GHRepository repository = gitHub.getRepository(gitConfig.getParentRepository());
        Map<String, String> webhookConfig = gitConfig.getWebhookConfig();

        if (repository.getHooks().size() == 0) {
            repository.createHook(WEB_HOOK_PREFIX, webhookConfig,
                    Collections.singletonList(GHEvent.PUSH), true);
        }

        webhookConfig.put("url", HOOK_URL);
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
