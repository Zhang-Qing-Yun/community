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
 * @since 2021-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String username;

    private String password;

    private String email;

    private Integer type;

    private Integer status;

    private String activationCode;

    private String headerUrl;

    private Date createTime;


}
