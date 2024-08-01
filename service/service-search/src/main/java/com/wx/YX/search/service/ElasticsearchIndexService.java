package com.wx.YX.search.service;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ElasticsearchIndexService {

    @Autowired
    private RestHighLevelClient client;

    @PostConstruct
    public void createIndex() {
        try {
            IndicesClient indicesClient = client.indices();
            CreateIndexRequest request = new CreateIndexRequest("skues");

            request.settings(Settings.builder()
                    .put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 1)
            );

            String mappingJson = "{\n" +
                    "  \"properties\": {\n" +
                    "    \"id\": {\"type\": \"long\"},\n" +
                    "    \"keyword\": {\"type\": \"text\", \"analyzer\": \"ik_max_word\"},\n" +
                    "    \"skuType\": {\"type\": \"integer\"},\n" +
                    "    \"isNewPerson\": {\"type\": \"integer\"},\n" +
                    "    \"categoryId\": {\"type\": \"long\"},\n" +
                    "    \"categoryName\": {\"type\": \"text\"},\n" +
                    "    \"imgUrl\": {\"type\": \"keyword\"},\n" +
                    "    \"title\": {\"type\": \"text\"},\n" +
                    "    \"price\": {\"type\": \"double\"},\n" +
                    "    \"stock\": {\"type\": \"integer\"},\n" +
                    "    \"perLimit\": {\"type\": \"integer\"},\n" +
                    "    \"sale\": {\"type\": \"integer\"},\n" +
                    "    \"wareId\": {\"type\": \"long\"},\n" +
                    "    \"hotScore\": {\"type\": \"long\"},\n" +
                    "    \"ruleList\": {\"type\": \"object\"}\n" +
                    "  }\n" +
                    "}";

            request.mapping(mappingJson, XContentType.JSON);

            CreateIndexResponse createIndexResponse = indicesClient.create(request, RequestOptions.DEFAULT);
            if (createIndexResponse.isAcknowledged()) {
                System.out.println("Index created successfully.");
            } else {
                System.out.println("Index creation failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
