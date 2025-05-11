package com.media.socialmedia.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.media.socialmedia.Repository.search")
public class ElasticConfig extends ElasticsearchConfiguration {
    @Value("${spring.data.elasticsearch.host}")
    private String host;
    @Value("${spring.data.elasticsearch.port}")
    private int port;
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo("%s:%d".formatted(host,port))
//              .withBasicAuth() if you use auth
                .build();
    }
}
