package com.qingyun.community.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.qingyun.community.search.config.ESConfig;
import com.qingyun.community.search.feignClient.UserClient;
import com.qingyun.community.search.pojo.Page;
import com.qingyun.community.search.pojo.Post;
import com.qingyun.community.search.service.ElasticsearchService;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-22 14:03
 **/
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${elasticsearch.index.post}")
    private String index;

    @Value("${elasticsearch.type.post}")
    private String type;

    private final int SLEEP_TIME = 2*1000;

    @Override
    public void addPostToES(Post post) {
        IndexRequest request = new IndexRequest(index, type, String.valueOf(post.getId()));
        //  将对象转换成字符串
        String json = JSON.toJSONString(post);
        request.source(json, XContentType.JSON);
        //  异步向ES保存一条数据
        restHighLevelClient.indexAsync(request, ESConfig.COMMON_OPTIONS, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                //  保存成功

            }

            @Override
            public void onFailure(Exception e) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                //  保存失败则重新尝试
                addPostToES(post);
            }
        });
    }

    @Override
    public void deletePostFromES(int id) {
        DeleteRequest request = new DeleteRequest(index, type, String.valueOf(id));
        //  异步删除
        restHighLevelClient.deleteAsync(request, ESConfig.COMMON_OPTIONS, new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {

            }

            @Override
            public void onFailure(Exception e) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                //  失败重试
                deletePostFromES(id);
            }
        });
    }

    @Override
    public Page<Post> searchFromES(String keyword, Integer current, Integer pageSize) throws IOException {
        //  要返回的分页对象
        Page<Post> page = new Page<>();
        //  检索请求
        SearchRequest searchRequest = new SearchRequest(index);
        //  构造查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "title", "content"));
        //  分页
        searchSourceBuilder.from(current);
        searchSourceBuilder.size(pageSize);
        //  超时时长
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //  排序
        searchSourceBuilder.sort("type", SortOrder.DESC);
        searchSourceBuilder.sort("score", SortOrder.DESC);
        searchSourceBuilder.sort("createTime", SortOrder.DESC);
        //  高亮
        searchSourceBuilder.highlighter(new HighlightBuilder().field("title").field("content").preTags("<em>").postTags("</em>"));

        //  执行查询
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ESConfig.COMMON_OPTIONS);
        //  分装分页对象
        SearchHits hits = searchResponse.getHits();
        //  总记录数
        page.setTotal(hits.getTotalHits());
        //  当前页帖子数据
        List<Post> list = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Post post = new Post();
            Map<String, Object> map = hit.getSourceAsMap();

            if (map.get("id") != null) {
                String id = map.get("id").toString();
                post.setId(Integer.valueOf(id));
            }

            if(map.get("userId") != null) {
                String userId = map.get("userId").toString();
                post.setUserId(userId);
            }


            if(map.get("title") != null) {
                String title = map.get("title").toString();
                post.setTitle(title);
            }


            if(map.get("content") != null) {
                String content = map.get("content").toString();
                post.setContent(content);
            }


            if(map.get("status") != null) {
                String status = map.get("status").toString();
                post.setStatus(Integer.valueOf(status));
            }


            if(map.get("createTime") != null) {
                String createTime = map.get("createTime").toString();
                post.setCreateTime(new Date(Long.parseLong(createTime)));
            }


            if(map.get("commentCount") != null) {
                String commentCount = map.get("commentCount").toString();
                post.setCommentCount(Integer.valueOf(commentCount));
            }


            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                post.setTitle(titleField.getFragments()[0].toString());
            }

            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                post.setContent(contentField.getFragments()[0].toString());
            }

            list.add(post);
        }
        page.setItems(list);
        return page;
    }


}
