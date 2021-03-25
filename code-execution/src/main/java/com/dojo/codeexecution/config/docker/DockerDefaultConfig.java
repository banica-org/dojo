package com.dojo.codeexecution.config.docker;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerDefaultConfig {

    @Bean
    DockerClientConfig dockerDefaultClientConfig(){
        return DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    }
}
