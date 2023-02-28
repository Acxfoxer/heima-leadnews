package com.heima.minio.service;

import java.io.InputStream;

/**
 * @author LEI
 */
public interface FileStorageService {
    /**
     *  上传图片文件
     * @param prefix  文件前缀
     * @param fileName  文件名
     * @param inputStream 文件流
     * @return  文件全路径
     */
    String uploadImgFile(String prefix, String fileName, InputStream inputStream);

    /**
     *  上传html文件
     * @param prefix  文件前缀
     * @param fileName   文件名
     * @param inputStream  文件流
     * @return  文件全路径
     */
    public String uploadHtmlFile(String prefix, String fileName,InputStream inputStream);

    /**
     * 删除文件
     * @param pathUrl  文件全路径
     */
    public void delete(String pathUrl);

    /**
     * 下载文件
     * @param pathUrl  文件全路径
     * @return
     *
     */
    public byte[]  downLoadFile(String pathUrl);
}
