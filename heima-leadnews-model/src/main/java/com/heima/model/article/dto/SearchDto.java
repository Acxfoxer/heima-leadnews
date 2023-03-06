package com.heima.model.article.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * es搜索参数
 * @author 18727
 */
@Data
public class SearchDto {
    @ApiModelProperty(value = "equipmentId",required = false)
    private String equipmentId;
    /**
     * 每页显示条数
     */
    @ApiModelProperty(value = "每页显示条数",required = true)
    private Integer pageSize;
    /**
     * 当前页码
     */
    @ApiModelProperty(value = "当前页码",required = true)
    private Integer pageNum;
    /**
     * 搜索内容分类
     */
    @ApiModelProperty(value = "内容分类条件",name = "tag",required = true)
    private String tag;
    /**
     * 搜索字段
     */
    @ApiModelProperty(value = "搜索字段",required = true)
    private String searchWords;
    /**
     * 最小时间
     */
    @ApiModelProperty(value = "最小时间",required = true)
    Date minBehotTime;

}
