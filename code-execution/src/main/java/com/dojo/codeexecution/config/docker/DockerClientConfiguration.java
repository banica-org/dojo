package com.dojo.codeexecution.config.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientConfiguration {

    @Bean
    public DockerClient createDockerClient() {
        return DockerClientImpl.getInstance(dockerDefaultClientConfig(), httpClient());
    }

    @Bean
    DockerClientConfig dockerDefaultClientConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    }

    @Bean
    DockerHttpClient httpClient() {
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerDefaultClientConfig().getDockerHost())
                .build();
    }
}
