package com.heima.article;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.service.ApArticleService;
import com.heima.minio.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ArticleAppTest {
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private Configuration configuration;
    @Autowired
    private ApArticleService apArticleService;
    @Resource
    ApArticleContentMapper contentMapper;
    @Test
    public void createStaticUrlTest() throws Exception {
        //1.获取文章内容
        ApArticleContent apArticleContent = contentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, 1383827888816836609L));
        if(apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())){
            //2.文章内容通过freemarker生成html文件
            StringWriter out = new StringWriter();
            Template template = configuration.getTemplate("article.ftl");

            Map<String, Object> params = new HashMap<>();
            JSONArray value = JSONArray.parseArray(apArticleContent.getContent());
            params.put("content", value);
            System.out.println(value);
            template.process(params, new FileWriter("d:/s123.html"));

        }
    }

    @Test
    public void test(){
        //Date date = new Date(1618762817000L);   //2021-04-19 00:20:19
        Date date = new Date(1599489079000L);      //2020-09-07 22:31:07
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
        String format = sim.format(date);
        System.out.println(format);
    }
}
