package com.heima.user.web.v1;


import com.heima.common.exceptionHandle.CustomException;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.LoginDto;
import com.heima.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
@Api(value = "用户登录",tags = "用户登录")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation("用户登录")
    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto dto) throws CustomException {
        return userService.login(dto);
    }
}
