package com.heima.minio;

import com.heima.minio.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


@SpringBootTest
public class ApplicationTest {
    @Resource
    FileStorageService fileStorageService;


    @Test
    public void test() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("D:\\Normal_software\\Baiduyun\\BaiduDownload\\09-项目三黑马头条+无虚拟机" +
                "\\09-项目三黑马头条\\day02-app端文章查看，静态化freemarker,分布式文件系统minIO\\资料\\模板文件\\plugins\\css\\index.css");
        String ss = fileStorageService.uploadHtmlFile("", "index.html", fileInputStream);
        System.out.println(ss);
    }

    /**
     * MinIO测试
     * @throws Exception 抛出异常
     */
    @Test
    public void test1() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("D:\\Normal_software\\Baiduyun\\BaiduDownload\\09-项目三黑马头条+无虚拟机" +
                "\\09-项目三黑马头条\\day02-app端文章查看，静态化freemarker,分布式文件系统minIO\\资料\\模板文件\\plugins\\js\\index.js");
        //创建MinIO客户端
        MinioClient minioClient = MinioClient.builder().credentials("lee","1829047yy")
                .endpoint("http://192.168.136.101:9000").build();
        //上传
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .stream(fileInputStream,fileInputStream.available(),-1)
                .object("plugins/js/index.js")
                .contentType("text/css")
                .bucket("leadnews")
                .build();
        minioClient.putObject(putObjectArgs);
    }
}
