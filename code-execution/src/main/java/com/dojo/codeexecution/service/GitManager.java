package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.github.GitConfigProperties;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GitManager {

    private static final String WEB_HOOK_PREFIX = "web";
    private static final String REPO_PREFIX = "gamified-hiring";
    private static final String PARENT_HOOK = "build";

    private final GitConfigProperties gitConfig;
    private final GitHub gitHub;

    @Autowired
    public GitManager(GitConfigProperties gitConfig, GitHub gitHub) {
        this.gitConfig = gitConfig;
        this.gitHub = gitHub;
    }

    @PostConstruct
    public void buildParentWebHook() throws IOException {
        String repositoryPath = gitConfig.getUser() + "/" + gitConfig.getParentRepositoryName();
        GHRepository repository = gitHub.getRepository(repositoryPath);
        if (repository.getHooks().size() == 0) {
            Map<String, String> webhookConfig = new HashMap<>(gitConfig.getWebhookConfig());
            webhookConfig.put("url", gitConfig.getWebhookAddress() + PARENT_HOOK);

            repository.createHook(WEB_HOOK_PREFIX, webhookConfig,
                    Collections.singletonList(GHEvent.PUSH), true);
        }
    }

    @Retryable
    public URL getExistingGitHubRepository(String username, String game) throws IOException {
        return gitHub.getRepository(getRepositoryNameByOwner(username, game)).getHtmlUrl();
    }

    @Retryable
    public URL createGitHubRepository(String username, String game) throws IOException {
        GHUser user = getGitHubUser(username);
        String repoName = REPO_PREFIX + "/" + user.getLogin() + "-" + game;

        GHRepository repo = gitHub.createRepository(repoName).private_(true).create();

        repo.createHook(WEB_HOOK_PREFIX, gitConfig.getWebhookConfig(),
                Collections.singletonList(GHEvent.PUSH), true);
        repo.addCollaborators(user);

        return repo.getHtmlUrl();
    }

    public boolean hasUserExistingRepository(String username, String game) {
        try {
            gitHub.getRepository(getRepositoryNameByOwner(username, game));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private String getRepositoryNameByOwner(String username, String game) throws IOException {
        return gitHub.getMyself().getLogin() + "/" + REPO_PREFIX + "-"
                + getGitHubUser(username).getLogin() + "-" + game;
    }

    private GHUser getGitHubUser(String username) throws IOException {
        return gitHub.getUser(username);
    }
}
