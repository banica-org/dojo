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

    final static Logger logger = LoggerFactory.getLogger(RequestReceiver.class);

    private static final String WEB_HOOK_PREFIX = "web";
    private static final String REPO_PREFIX = "gamified-hiring";

    private final GitConfigProperties gitConfig;
    private final GitHub gitHub;

    @Autowired
    public GitManager(GitConfigProperties gitConfig, GitHub gitHub) {
        this.gitConfig = gitConfig;
        this.gitHub = gitHub;
    }

    private boolean hasUserExistingRepository(GHUser user){
        try{
            gitHub.getRepository(getRepoNameWithOwner(user));
        }catch(IOException e){
            return false;
        }
        return true;
    }

    @Retryable
    public String getRepository(String username) {
        try{
            GHUser ghUser = getGhUser(username);
            return createRepository(ghUser).toString();
        }catch (Exception e){
            logger.error(e.getMessage());
            return "User not found!";
        }
    }

    private URL createRepository(GHUser user) throws IOException {
        if(hasUserExistingRepository(user)){
            return gitHub.getRepository(getRepoNameWithOwner(user)).getHtmlUrl();
        }
        return createGitRepo(gitHub, user);
    }

    private String getRepoNameWithOwner(GHUser ghUser) throws IOException {
        return gitHub.getMyself().getLogin() + "/" + REPO_PREFIX + "-" + ghUser.getLogin();
    }

    private GHUser getGhUser(String username) throws IOException {
        List<GHUser> users = gitHub.searchUsers().q(username).list().toList();
        if (users.size() == 1) {
            return users.get(0);
        }
        throw new IllegalArgumentException("User not found!");
    }

    private URL createGitRepo(GitHub gitHub, GHUser user) throws IOException {
        String repoName = REPO_PREFIX + "/" + user.getLogin();

        GHRepository repo = gitHub.createRepository(repoName).private_(true).create();

        repo.createHook(WEB_HOOK_PREFIX, gitConfig.getWebhookConfig(),
               Collections.singletonList(GHEvent.PUSH), true);

        repo.addCollaborators(user);

        return repo.getHtmlUrl();
    }

}
