package com.heima.model.search.vos;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author 18727
 */
@Data
public class SearchArticleVo {
    /**
     * 文章id
     */
    private Long id;
    /**
     * 文章标题
     */
    private String title;
    /**
     * 文章发布时间
     */
    private Date publishTime;
    /**
     * 文章布局
     */
    private Integer layout;
    /**
     * 文章图片
     */
    private String images;
    /**
     * 作者id
     */
    private Long authorId;
    /**
     * 作者名字
     */
    private String authorName;
    /**
     * 静态页面url
     */
    private String staticUrl;
    /**
     * 文章内容
     */
    private String content;
    /**
     * 高亮结果
     */
    String h_title;
    /**
     * 自动补全结果
     */
    List<String> suggestion;
}