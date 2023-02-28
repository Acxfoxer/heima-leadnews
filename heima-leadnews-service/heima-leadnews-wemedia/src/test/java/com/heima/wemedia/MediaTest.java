package com.heima.wemedia;

import com.alibaba.nacos.shaded.org.checkerframework.checker.units.qual.A;
import com.heima.model.media.pojos.WmMaterial;
import com.heima.wemedia.mapper.WmMaterialMapper;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class MediaTest {
    @Autowired
    MinioClient client;
    @Autowired
    WmMaterialMapper mapper;

    /**
     * 插入数据库图片链接
     * @throws Exception
     */
    @Test
    public void insertImage() throws Exception{
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket("leadnews")
                        .startAfter("2023/02/27")
                .recursive(true)
                .build());
        String url = "http://192.168.136.101:9000/leadnews/2023/02/27";
        for (Result<Item> result : results) {
            Item item = result.get();
            String str = item.objectName();
            WmMaterial wmMaterial = new WmMaterial();
            wmMaterial.setCreatedTime(new Date());
            wmMaterial.setUserId(1102);
            wmMaterial.setType((short) 0);
            wmMaterial.setIsCollection((short) 0);
            wmMaterial.setUrl(url+str);
            mapper.insert(wmMaterial);
        }
    }
}
