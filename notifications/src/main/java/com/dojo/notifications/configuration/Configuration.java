package com.dojo.notifications.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class Configuration {
    private String leaderboardApi;
    private String userDetailsApi;
    private int threadPoolSchedulePeriodSeconds;
    private int threadPoolSize;

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void setLeaderboardApi(String leaderboardApi) {
        this.leaderboardApi = leaderboardApi;
    }

    public int getThreadPoolSchedulePeriodSeconds() {
        return threadPoolSchedulePeriodSeconds;
    }

    public void setThreadPoolSchedulePeriodSeconds(int threadPoolSchedulePeriodSeconds) {
        this.threadPoolSchedulePeriodSeconds = threadPoolSchedulePeriodSeconds;
    }

    public String getLeaderboardApi() {
        return leaderboardApi;
    }

    public String getUserDetailsApi() {
        return userDetailsApi;
    }

    public void setUserDetailsApi(String userDetailsApi) {
        this.userDetailsApi = userDetailsApi;
    }
}

