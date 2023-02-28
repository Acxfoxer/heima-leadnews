package com.heima.model.article.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
public class ArticleHomeDto {
    /**
     * 显示条数
     */
    @ApiModelProperty(value = "显示条数",required = true)
    Integer size;
    /**
     * 频道id
     */
    @ApiModelProperty(value = "频道id",required = true)
    String tag;
    /**
     * 模式
     */
    @ApiModelProperty(value = "模式",required = true)
    Integer loaddir;
    /**
     * 菜单选项
     */
    @ApiModelProperty(value = "菜单选项",required = true)
    Integer index;
    /**
     * 最大时间
     */
    @ApiModelProperty(value = "最大时间",required = true)
    Date  maxBehotTime;
    /**
     * 最小时间
     */
    @ApiModelProperty(value = "最小时间",required = true)
    Date minBehotTime;
}
