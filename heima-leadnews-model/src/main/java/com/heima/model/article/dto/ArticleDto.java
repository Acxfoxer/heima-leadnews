package com.heima.model.article.dto;

import com.heima.model.article.pojos.ApArticle;
import lombok.Data;
/**
 * @author 18727
 */
@Data
public class ArticleDto extends ApArticle{
    /**
     * 文章内容
     */
    private String content;
    /**
     * wm_user_id
     */
    private Long wmUserId;
}
