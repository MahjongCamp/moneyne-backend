package org.mahjongcamp.moneynebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("email")
public class Email {

    @TableId(type = IdType.NONE)
    private Integer id;

    private String fromAdd;

    private String pass;

    private String emailType;

    private String host;

    private String user;

    private Integer port;
}
