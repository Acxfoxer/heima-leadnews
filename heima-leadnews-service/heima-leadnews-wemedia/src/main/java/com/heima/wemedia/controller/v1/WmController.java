package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dto.WmMaterialDto;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1")
public class WmController {
    @Resource
    private WmChannelService wmChannelService;
    @Autowired
    WmMaterialService materialService;
    /**
     * 查询全部频道接口
     * @return
     */
    @GetMapping("/channel/channels")
    public ResponseResult getChannel(){
        return ResponseResult.okResult(wmChannelService.list());
    }
    /**
     * 分页
     * @param dto 前端传递参数
     * @return
     */
    @PostMapping("/material/list")
    public PageResponseResult list(@RequestBody WmMaterialDto dto){
        return materialService.listByPage(dto);
    }

    /**
     * 图片上传
     * @param multipartFile 图片二进制数据
     * @return
     */
    @PostMapping("/material/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
       return materialService.uploadPicture(multipartFile);
    }

    /**
     * 收藏图片
     * @param id
     * @return
     */
    @GetMapping("/material/collect/{id}")
    public ResponseResult collect(@PathVariable("id")Long id){
        return materialService.collect(id);
    }

    /**
     * 删除图片
     * @param id
     */
    @GetMapping("/material/del_picture/{id}")
    public ResponseResult deletePicture(@PathVariable("id")Long id){
        return materialService.deletePicture(id);
    }
}
