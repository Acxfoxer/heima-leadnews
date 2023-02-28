package com.heima.article.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dto.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApArticleMapper extends BaseMapper<ApArticle> {

    List<ApArticle> loadArticleList(@Param("dto") ArticleHomeDto dto);

}
