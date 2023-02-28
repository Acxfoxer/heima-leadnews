package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.LoginDto;
import com.heima.model.user.pojos.ApUser;

public interface UserService extends IService<ApUser> {
    /**
     * 用户登录验证接口
     * @param dto 前端传递参数
     * @return 返回用户数据
     */
    public ResponseResult login(LoginDto dto);
}
