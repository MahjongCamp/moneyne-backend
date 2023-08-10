package org.mahjongcamp.moneynebackend.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import org.mahjongcamp.moneynebackend.entity.User;
import org.mahjongcamp.moneynebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private UserService service;

    @RequestMapping("test")
    public String test(){
        return "hello world";
    }

    @RequestMapping("isLogin")
    public String isLogin(){
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    //检查用户名是否被使用
    @PostMapping("checkoutUsername")
    public SaResult checkoutUsername(@RequestBody User user){
        User userByName = service.findUserByName(user.getUsername());
        if (userByName != null) return SaResult.error("用户名已被使用！");
        return SaResult.ok("用户名可以使用！");
    }

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

    @RequestMapping("logout")
    public SaResult logout(){
        StpUtil.logout();
        return SaResult.ok("注销成功");
    }
}
