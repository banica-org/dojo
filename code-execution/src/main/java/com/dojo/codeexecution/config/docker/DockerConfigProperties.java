package com.dojo.codeexecution.config.docker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerConfigProperties {

    private String filepath;
    private String parentTag;
    private List<String> shellArguments;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getParentTag() {
        return parentTag;
    }

    public void setParentTag(String parentTag) {
        this.parentTag = parentTag;
    }

    public List<String> getShellArguments() {
        return shellArguments;
    }

    public void setShellArguments(List<String> shellArguments) {
        this.shellArguments = shellArguments;
    }
}
