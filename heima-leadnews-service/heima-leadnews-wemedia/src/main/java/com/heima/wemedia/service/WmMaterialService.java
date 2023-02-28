package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dto.WmMaterialDto;
import com.heima.model.media.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {
    /**
     * 分页查询
     * @param dto
     * @return
     */
    public PageResponseResult listByPage(WmMaterialDto dto);

    /**
     * 图片上传到minio
     * @param multipartFile 图片二进制数据
     * @return
     */
    ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 图片收藏
     * @param id
     * @return
     */
    ResponseResult collect(Long id);

    /**
     * 删除图片
     * @param id
     */
    ResponseResult deletePicture(Long id);
}
