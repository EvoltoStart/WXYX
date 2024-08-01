package com.wx.YX.search.config;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")  // Replace with your Elasticsearch host and port
                )
        );
    }
}
