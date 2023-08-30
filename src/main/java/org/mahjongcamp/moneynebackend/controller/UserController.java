package org.mahjongcamp.moneynebackend.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mahjongcamp.moneynebackend.entity.User;
import org.mahjongcamp.moneynebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

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
    @Operation(summary = "检查邮箱是否可用",description = "检查邮箱是否可用")
    @PostMapping("checkoutEmailAdd")
    public SaResult checkoutEmailAdd(@RequestBody User user){
        User userByEmail = service.findUserByEmail(user.getUsername());
        if (userByEmail != null) return SaResult.error("邮箱已被使用！");
        return SaResult.ok("邮箱可以使用！");
    }

    @Operation(summary = "注册",description = "注册")
    @PostMapping("signIn")
    public SaResult signIn(@RequestBody User user) {
        if (StrUtil.isBlank(user.getEmailAdd())
                &&StrUtil.isBlank(user.getPassword())
                &&StrUtil.isBlank(user.getEmailAdd())) {
            return SaResult.error("邮箱密码不能为空");
        }
        User userInfo = service.sign(user);
        userInfo.setPassword(null);
        Map<String, Object> map = new HashMap<>();
        map.put("token",StpUtil.getTokenInfo().getTokenValue());
        map.put("userInfo", new JSONObject(userInfo));
        return SaResult.data(map);
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
        User userInfo = service.login(user);
        userInfo.setPassword(null);
        Map<String, Object> map = new HashMap<>();
        map.put("token",StpUtil.getTokenInfo().getTokenValue());
        map.put("userInfo", new JSONObject(userInfo));
        return SaResult.data(map);
    }

    @Operation(summary = "登出",description = "登出")
    @RequestMapping("logout")
    public SaResult logout(){
        StpUtil.logout();
        return SaResult.ok("注销成功");
    }

    @Operation(summary = "修改密码")
    @PostMapping("modifyPassword")
    public SaResult modifyPassword(@RequestBody User user) {
        service.modifyPassword(user);
        return SaResult.ok("修改成功");
    }

    @Operation(summary = "修改用户名")
    @PostMapping("modifyUsername")
    public SaResult modifyUsername(@RequestBody User user) {
        service.modifyUsername(user);
        return SaResult.ok("修改成功");
    }

    @Operation(summary = "忘记密码")
    @PostMapping("forgetPass")
    public SaResult forgetPass(@RequestBody User user) {
        service.forgetPass(user);
        return SaResult.ok("修改成功");
    }
}
