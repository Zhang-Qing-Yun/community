package com.qingyun.community.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description： 自定义异常，用于出错时返回JSON数据，而不是错误页面的场景
 * @author: 張青云
 * @create: 2021-05-23 13:04
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityException extends RuntimeException{
    private Integer code;
    private String msg;
}
