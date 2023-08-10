package org.mahjongcamp.moneynebackend.entity;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;

    private String username;

    private String password;

    private String emailAdd;

    private Integer phoneNumber;

    private String avatarUrl;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public User getCurrentUser(){
        return (User) StpUtil.getSession().get("user");
    }
}
