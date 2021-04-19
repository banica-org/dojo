package com.dojo.codeexecution.config.github;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "git")
public class GitConfigProperties {

    private int contentType;
    private String user;
    private String tokenCreateRepo;
    private String parentRepositoryName;
    private String webhookAddress;
    private Map<String, String> webhookConfig;

    public String getRepoToken() {
        return tokenCreateRepo;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public String getTokenCreateRepo() {
        return tokenCreateRepo;
    }

    public void setTokenCreateRepo(String tokenCreateRepo) {
        this.tokenCreateRepo = tokenCreateRepo;
    }

    public Map<String, String> getWebhookConfig() {
        return webhookConfig;
    }

    public void setWebhookConfig(Map<String, String> webhookConfig) {
        this.webhookConfig = webhookConfig;
    }

    public String getWebhookAddress() {
        return webhookAddress;
    }

    public void setWebhookAddress(String webhookAddress) {
        this.webhookAddress = webhookAddress;
    }

    public String getParentRepositoryName() {
        return parentRepositoryName;
    }

    public void setParentRepositoryName(String parentRepositoryName) {
        this.parentRepositoryName = parentRepositoryName;
    }
}
