package com.heima.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exceptionHandle.CustomException;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dto.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.UserMapper;
import com.heima.user.service.UserService;
import com.heima.utils.common.AppJwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, ApUser>  implements UserService {
    /**
     * 用户登录验证接口
     * @param dto 前端传递参数
     * @return 返回用户数据
     */
    @Override
    public ResponseResult login(LoginDto dto) throws CustomException {
        //封装结果数据
        Map<String,Object> map = new HashMap<>();
       //判断账号跟密码是否为空,是采用游客还是用户登录
        if(StringUtils.isNotBlank(dto.getPhone())&&StringUtils.isNotBlank(dto.getPassword())){
            //不为空,根据手机号查询用户
            LambdaQueryWrapper<ApUser> lqw = new LambdaQueryWrapper<>();
            lqw.eq(ApUser::getPhone,dto.getPhone());
            ApUser apUser = this.getOne(lqw);
            //判断用户是否存在
            if(apUser==null){
                return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST,"用户不存在");
            }
            //比较密码
            String password = dto.getPassword();
            String salt = apUser.getSalt();
            String pw = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if(!pw.equals(apUser.getPassword())){
                //密码不等则,返回账号密码错误
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            //正确,返回数据,加token
            String token = AppJwtUtil.getToken(apUser.getId().longValue());
            map.put("token",token);
            //清空用户salt跟密码信息
            apUser.setSalt("");
            apUser.setPassword("");
            map.put("user",apUser);
        }else {
            //为空,即没输入数据,选择游客登录,即id为0
            String token = AppJwtUtil.getToken(0L);
            map.put("token",token);
        }
        return ResponseResult.okResult(map);
    }
}
