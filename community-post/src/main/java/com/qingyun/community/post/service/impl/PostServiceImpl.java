package com.qingyun.community.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.mapper.PostMapper;
import com.qingyun.community.post.service.PostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-02
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Value("${page.size}")
    private int PAGE_SIZE;

    @Override
    public Map<String, Object> getPost(Integer current, Integer userId) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        if(current == null || current <= 1) {
            current = 1;
        }
        Page<Post> page = new Page<>(current, PAGE_SIZE);
        Map<String, Object> map = new HashMap<>();

        if(userId != null) {
            wrapper.eq("user_id", userId);
        }
        baseMapper.selectPage(page, wrapper);
        //  该页的记录
        List<Post> items = page.getRecords();
        //  总页数
        long pages = page.getPages();
        //  每页记录数
        long size = page.getSize();
        //  总记录数
        long total = page.getTotal();
        //  是否有下一页
        boolean hasNext = page.hasNext();
        //  是否有上一页
        boolean hasPrevious = page.hasPrevious();


        map.put("items", items);
        map.put("current", current);
        map.put("pages", pages);
        map.put("pageSize", size);
        map.put("total", total);
        map.put("hasNext", hasNext);
        map.put("hasPrevious", hasPrevious);
        return map;
    }
}
