package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author 18727
 */
@Data
@TableName("ap_author")
public class ApAuthor {
    @TableId(type = IdType.AUTO)
    private Short id;
    private String name;
    private Short type;
    @TableField(value = "user_id")
    private Short userId;
    @TableField("created_time")
    private Date createdTime;
    @TableField("wm_user_id")
    private Long wmUserId;
}
