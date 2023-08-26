package org.mahjongcamp.moneynebackend.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mahjongcamp.moneynebackend.entity.User;
import org.mahjongcamp.moneynebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/user/")
@Tag(name = "用户接口")
public class UserController {

    @Autowired
    private UserService service;

    @Operation(summary = "当前会话是否登录")
    @RequestMapping("isLogin")
    public String isLogin(){
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    //检查用户名是否被使用
    @Operation(summary = "检查用户名是否被使用",description = "检查用户名是否被使用")
    @PostMapping("checkoutUsername")
    public SaResult checkoutUsername(@RequestBody User user){
        User userByName = service.findUserByName(user.getUsername());
        if (userByName != null) return SaResult.error("用户名已被使用！");
        return SaResult.ok("用户名可以使用！");
    }

    @Operation(summary = "注册",description = "注册")
    @PostMapping("signIn")
    public SaResult signIn(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername())
                &&StrUtil.isBlank(user.getPassword())
                &&StrUtil.isBlank(user.getEmailAdd())) {
            return SaResult.error("用户名密码不能为空");
        }
        service.sign(user);
        return SaResult.ok("注册成功");
    }

    @Operation(summary = "发送邮箱验证码",description = "发送邮箱验证码")
    @PostMapping("sendVerifyCode")
    public SaResult sendVerifyCode(@RequestBody User user) throws GeneralSecurityException {
        service.sendVerifyCode(user);
        return SaResult.ok();
    }

    @Operation(summary = "登录",description = "登录")
    @PostMapping("login")
    public SaResult login(@RequestBody User user) {
        Boolean flag = service.login(user);
        if (flag){
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return SaResult.data(tokenInfo);
        }else {
            return SaResult.error("用户名或密码错误");
        }
    }

    @Operation(summary = "登出",description = "登出")
    @RequestMapping("logout")
    public SaResult logout(){
        StpUtil.logout();
        return SaResult.ok("注销成功");
    }
}
