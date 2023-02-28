package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.media.dto.WmNewsPageReqDto;
import com.heima.model.media.pojos.WmNews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WmNewsMapper extends BaseMapper<WmNews> {

}
