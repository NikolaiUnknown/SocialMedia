package com.media.socialmedia.Configs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.media.socialmedia.Repository.search")
public class ElasticConfig extends ElasticsearchConfiguration {
    @Value("${spring.data.elasticsearch.url}")
    private String url;
    @Value("${spring.data.elasticsearch.username}")
    private String username;
    @Value("${spring.data.elasticsearch.password}")
    private String password;

    @PostConstruct
    void initIndex() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        String indexName = "users";
        String url = "http://" + this.url + "/" + indexName;
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Users index is already created");
                return;
            }
        } catch (HttpClientErrorException e) {
            log.info("Creating new index");
        }
        try {
            String json = new String(Files.readAllBytes(Paths.get("init/elasticsearch/users.json")));
            headers.setContentType(MediaType.APPLICATION_JSON);
            entity = new HttpEntity<>(json, headers);
            response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Users index successfully created");
            } else {
                log.error("Error with index creation");
                throw new RuntimeException("Error with index creation");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error with index creation: " + e);
        }
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(url)
                .withBasicAuth(username,password)
                .build();
    }

    @Bean(name = "bucketSearch")
    public Bucket bucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(1)
                .refillGreedy(1, Duration.ofSeconds(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
