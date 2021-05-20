package com.dojo.codeexecution.config.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Bean(destroyMethod = "shutdownNow")
    @Qualifier("buildImageSingleThreadExecutor")
    ExecutorService singleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
