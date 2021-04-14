package com.dojo.codeexecution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "codenjoy")
public class CodenjoyConfigProperties {

    private String pointsUpdateUrlStart;
    private String pointsUpdateUrlTail;

    public String getPointsUpdateUrlStart() {
        return pointsUpdateUrlStart;
    }

    public void setPointsUpdateUrlStart(String pointsUpdateUrlStart) {
        this.pointsUpdateUrlStart = pointsUpdateUrlStart;
    }

    public String getPointsUpdateUrlTail() {
        return pointsUpdateUrlTail;
    }

    public void setPointsUpdateUrlTail(String pointsUpdateUrlTail) {
        this.pointsUpdateUrlTail = pointsUpdateUrlTail;
    }
}
