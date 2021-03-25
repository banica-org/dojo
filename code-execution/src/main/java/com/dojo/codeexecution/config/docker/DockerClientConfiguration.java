package com.dojo.codeexecution.config.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientConfiguration {

    @Bean
    public DockerClient createDockerClient(DockerClientConfig clientConfig, DockerHttpClient httpClient){
        return DockerClientImpl.getInstance(clientConfig, httpClient);
    }
}
