package com.qingyun.community.search.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author 張青云
 * @since 2021-05-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String userId;

    private String title;

    private String content;

    private Integer type;

    private Integer status;

    private Date createTime;

    private Integer commentCount;

    private Double score;
}
