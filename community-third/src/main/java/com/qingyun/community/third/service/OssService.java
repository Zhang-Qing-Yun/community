package com.qingyun.community.third.service;

import java.util.Map;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-06 14:37
 **/
public interface OssService {
    /**
     * 获取Oss签名token
     * @return
     */
    Map<String, String> getTokenOfOss();
}
