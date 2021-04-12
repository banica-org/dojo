package com.dojo.codeexecution.config;

import com.dojo.codeexecution.service.DockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collections;

@Component
public class BuildImageOnStartup {
    private final File dockerfile = new File("code-execution/src/main/docker/Dockerfile");
    @Autowired
    DockerService dockerService;

    @PostConstruct
    public void build() {
        dockerService.buildImage(dockerfile, Collections.singleton("user-param"),
                "giivanov722", "docker-test-parent");
    }
}
