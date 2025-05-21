package com.media.socialmedia.Configs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.media.socialmedia.Repository.search")
public class ElasticConfig extends ElasticsearchConfiguration {
    @Value("${spring.data.elasticsearch.url}")
    private String url;
    @Value("${spring.data.elasticsearch.username}")
    private String username;
    @Value("${spring.data.elasticsearch.password}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(url)
                .withBasicAuth(username,password)
                .build();
    }

    @Bean
    public Bucket bucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(1)
                .refillGreedy(1, Duration.ofSeconds(2))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
