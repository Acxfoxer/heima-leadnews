package com.heima.wemedia.service.impl;

import com.alibaba.fastjson2.util.UUIDUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.minio.config.MinIOConfigProperties;
import com.heima.minio.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.media.dto.WmMaterialDto;
import com.heima.model.media.pojos.WmMaterial;
import com.heima.utils.common.UserThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    MinIOConfigProperties properties;
    @Autowired
    MinioClient client;
    /**
     * 分页查询
     * @param dto 前端数据
     * @return
     */
    @Override
    public PageResponseResult listByPage(WmMaterialDto dto) {
        //参数校验
        dto.checkParam();
        dto.setSize(10);
        //构造条件
        IPage<WmMaterial> iPage = new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmMaterial> lqw = new LambdaQueryWrapper<>();
        //userId条件
        Long userId = UserThreadLocalUtils.get();
        lqw.eq(WmMaterial::getUserId, userId);
        //收藏条件
        lqw.eq(dto.getIsCollection()==1,WmMaterial::getIsCollection,dto.getIsCollection());
        this.page(iPage,lqw);
        PageResponseResult result = new PageResponseResult(dto.getPage(),dto.getSize(),(int) iPage.getTotal());
        result.setData(iPage.getRecords());
        return result;
    }

    /**
     * 图片上传到minio
     *
     * @param multipartFile 图片二进制数据
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //1.检查参数
        if(multipartFile == null || multipartFile.getSize() == 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        String fileUrl = null;
        try {
            boolean is_Exits = client.bucketExists(BucketExistsArgs.builder().bucket(properties
                    .getBucket()).build());
            //如果不存在,创建桶
            if(!is_Exits){
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            }
            //生成文件名
            String fileName = UUID.randomUUID().toString().replace("-", "");
            //获取原来文件名
            String originalFilename = multipartFile.getOriginalFilename();
            //获取源文件后缀
            String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
            fileUrl = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
        //创建数据
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUrl(fileUrl);
        wmMaterial.setType((short) 0);
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setCreatedTime(new Date());
        wmMaterial.setUserId(Math.toIntExact(UserThreadLocalUtils.get()));
        //写入数据库
        this.save(wmMaterial);
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 图片收藏
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult collect(Long id) {
        //构造更新条件
        UpdateWrapper<WmMaterial> uw = new UpdateWrapper<>();
        uw.eq(id!=null,"id",id);
        uw.set("is_collection",1);
        this.update(uw);
        return ResponseResult.okResult("收藏成功");
    }

    /**
     * 删除图片
     * @param id
     */
    @Override
    public ResponseResult deletePicture(Long id) {
        //构造条件
        boolean flag = this.removeById(id);
        if(flag){
            return ResponseResult.okResult("删除成功");
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }
}
