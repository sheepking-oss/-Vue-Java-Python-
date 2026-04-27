package com.contract.modules.system.controller;

import com.contract.common.result.Result;
import com.contract.modules.system.service.AuthService;
import com.contract.vo.LoginResultVO;
import com.contract.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/system/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginResultVO> login(@Validated @RequestBody LoginVO loginVO) {
        LoginResultVO result = authService.login(loginVO);
        return Result.success("登录成功", result);
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<LoginResultVO> getCurrentUserInfo() {
        LoginResultVO result = authService.getCurrentUserInfo();
        if (result == null) {
            return Result.unauthorized();
        }
        return Result.success(result);
    }
}
