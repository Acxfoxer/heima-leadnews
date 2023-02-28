package com.heima.minio.service;

import com.heima.minio.config.MinIOConfig;
import com.heima.minio.config.MinIOConfigProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author  Lei
 */
@Slf4j
@EnableConfigurationProperties(MinIOConfigProperties.class)
@Import(MinIOConfig.class)
@Service
public class FileStorageServiceImpl implements FileStorageService{
    private final MinioClient minioClient;
    private final MinIOConfigProperties minIOConfigProperties;

    private final static String separator = "/";

    public FileStorageServiceImpl(MinioClient minioClient, MinIOConfigProperties minIOConfigProperties) {
        this.minioClient = minioClient;
        this.minIOConfigProperties = minIOConfigProperties;
    }

    /**
     * 构建文件路径
     * @param dirPath   磁盘路径
     * @param filename  yyyy/mm/dd/file.jpg
     * @return 返回文件路径
     */
    public String builderFilePath(String dirPath,String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if(!StringUtils.isEmpty(dirPath)){
            stringBuilder.append(dirPath).append(separator);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(separator);
        stringBuilder.append(filename);
        return stringBuilder.toString();
    }
    /**
     * 上传图片文件
     * @param prefix      文件前缀
     * @param fileName    文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    @Override
    public String uploadImgFile(String prefix, String fileName, InputStream inputStream) {
        String filePath = builderFilePath(prefix, fileName);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType("image/jpg")
                    .bucket(minIOConfigProperties.getBucket()).stream(inputStream,inputStream.available(),-1)
                    .build();
            minioClient.putObject(putObjectArgs);
            return minIOConfigProperties.getReadPath() + separator + minIOConfigProperties.getBucket() +
                    separator + filePath;
        }catch (Exception ex){
            log.error("minio put file error.",ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 上传html文件
     * @param prefix      文件前缀
     * @param fileName    文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    @Override
    public String uploadHtmlFile(String prefix, String fileName, InputStream inputStream) {
        //获取文件路劲
        String filePath = builderFilePath(prefix, fileName);
        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .object(filePath)
                            .contentType("text/html")
                            .bucket(minIOConfigProperties.getBucket())
                            .stream(inputStream,inputStream.available(),-1)
                            .build()
            );
            return minIOConfigProperties.getReadPath() + separator + minIOConfigProperties.getBucket() +
                    separator + filePath;
        }catch (Exception e){
            log.error("minio put file error"+e);
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 删除文件
     *
     * @param pathUrl 文件全路径
     */
    @Override
    public void delete(String pathUrl) {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint()+"/","");
        int index = key.indexOf(separator);
        String bucket = key.substring(0,index);
        String filePath = key.substring(index+1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  pathUrl:{}",pathUrl);
            e.printStackTrace();
        }
    }

    /**
     * 下载文件
     * @param pathUrl 文件全路径
     * @return 返回字节数据
     */
    @Override
    public byte[] downLoadFile(String pathUrl) {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint()+"/","");
        int index = key.indexOf(separator);
        String filePath = key.substring(index+1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(minIOConfigProperties.getBucket()).object(filePath).build());
        } catch (Exception e) {
            log.error("minio down file error.  pathUrl:{}",pathUrl);
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                assert inputStream != null;
                if (!((rc = inputStream.read(buff, 0, 100)) > 0)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
