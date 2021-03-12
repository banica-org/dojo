package com.dojo.codeexecution.config;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GitConfiguration {

    @Bean
    GitHub createGitHub(GitConfigProperties gitConfig) throws IOException {
        return new GitHubBuilder().withOAuthToken(gitConfig.getRepoToken(), gitConfig.getUser()).build();
    }
}
