package com.qingyun.community.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-02 21:35
 **/
@Data
public class Page<T> {
    //  需要展示的页数，设为奇数
    private static final long showPageSize = 5;

    //  当前页码
    private int current;
    //  总页数
    private long pages;
    //  每页记录数
    private long size;
    //  总记录数
    private long total;
    //  是否有下一页
    private boolean hasNext;
    //  是否有上一页
    private boolean hasPrevious;
    //  从哪一页开始显示
    private long fromNum;
    //  显示到哪一页结束
    private long toNum;
    // 查询路径(用于复用分页链接)
    private String path;
    //  当前页的数据
    private List<T> items;

    public void setFrom() {
        long from = current-(showPageSize-1)/2;
        if(from >= 1) {
            this.fromNum = from;
        } else {
            this.fromNum = 1;
        }
    }

    public void setTo() {
        long to = current+(showPageSize-1)/2;
        if(to <= this.pages) {
            this.toNum = to;
        } else {
            this.toNum = this.pages;
        }
    }
}
