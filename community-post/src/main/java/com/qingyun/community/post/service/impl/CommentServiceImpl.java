package com.qingyun.community.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyun.community.base.component.SensitiveFilter;
import com.qingyun.community.post.pojo.Comment;
import com.qingyun.community.post.mapper.CommentMapper;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyun.community.post.service.PostService;
import com.qingyun.community.post.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-08
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Value("${page.size}")
    private int PAGE_SIZE;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private PostService postService;


    @Override
    public Map<String, Object> getCommentByEntityId(Integer current, Integer entityType, Integer entityId) {
        Map<String, Object> map = new HashMap<>();
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        //  按主键id降序，这样就保证了时间顺序
        wrapper.orderByDesc("id");
        wrapper.eq("entity_type", entityType);
        wrapper.eq("entity_id", entityId);

        // 如果current为null代表不分页
        if(current == null) {
            List<Comment> items = baseMapper.selectList(wrapper);
            map.put("items", items);
            return map;
        }
        if(current <= 1) {
            current = 1;
        }
        Page<Comment> page = new Page<>(current, PAGE_SIZE);
        baseMapper.selectPage(page, wrapper);

        //  该页的记录
        List<Comment> items = page.getRecords();
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

        //  封装分页对象
        com.qingyun.community.post.pojo.Page res = new com.qingyun.community.post.pojo.Page();
        res.setCurrent(current);
        res.setHasNext(hasNext);
        res.setHasPrevious(hasPrevious);
        res.setTotal(total);
        res.setSize(size);  // 每页记录数
        res.setPages(pages);  // 总页数
        res.setTo();
        res.setFrom();

        map.put("page", res);
        map.put("items", items);
        return map;
    }

    @Override
    @Transactional  // 因为添加评论涉及到的业务都是在post模块里的，不涉及到分布式事务，所以使用Spring的本地事务即可
    public void addComment(Comment comment) {
        if (comment == null){
            throw new  IllegalArgumentException("参数不能为空！");
        }
        //  处理评论的文本
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        baseMapper.insert(comment);

        if (comment.getEntityType() == Constant.ENTITY_TYPE_POST){
            //  查询当前数据库里该帖子的评论数
            QueryWrapper<Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("entity_type", comment.getEntityType());
            wrapper.eq("entity_id", comment.getEntityId());
            Integer count = baseMapper.selectCount(wrapper);
            //  更新帖子的评论数
            postService.updateCommentCount(comment.getEntityId(), count);
        }

    }
}
