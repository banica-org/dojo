package com.dojo.codeexecution.config.docker;

import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DockerHttpConfig {
    @Autowired
    DockerDefaultConfig dockerDefaultConfig;

    @Bean
    DockerHttpClient httpClient(){
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerDefaultConfig.dockerDefaultClientConfig().getDockerHost())
                .build();
    }
}
