package com.qingyun.community.search;

import com.alibaba.fastjson.JSON;
import com.qingyun.community.search.config.ESConfig;
import com.qingyun.community.search.pojo.Page;
import com.qingyun.community.search.pojo.Post;
import com.qingyun.community.search.service.ElasticsearchService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;

@SpringBootTest
class SearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ElasticsearchService esService;

    @Test
    void contextLoads() {
        System.out.println(client);
    }

    /**
     * 测试向ES存储数据
     */
    @Test
    void indexData() throws IOException {
//        for(int i = 0; i < 14; i++) {
//            IndexRequest indexRequest = new IndexRequest("test", "testType", i+"");
//            indexRequest.source("name", "Hello word!");
//            IndexResponse indexResponse = client.index(indexRequest, ESConfig.COMMON_OPTIONS);
//        }
        IndexRequest indexRequest = new IndexRequest("community_post", "post", "1");
        Post post = new Post();
        post.setContent("Hello word!");
        post.setTitle("hh");
        post.setId(1);
        post.setCommentCount(0);
        post.setStatus(0);
        post.setType(0);
        post.setScore(0.0);
        post.setUserId(125+"");
        post.setCreateTime(new Date());
        indexRequest.source(JSON.toJSONString(post), XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, ESConfig.COMMON_OPTIONS);
    }

    /**
     * 测试向ES存储数据
     */
    @Test
    void searchTest() throws IOException {
        Page<Post> page = esService.searchFromES("hello", 0, 5);
        System.out.println(page);

    }

}
