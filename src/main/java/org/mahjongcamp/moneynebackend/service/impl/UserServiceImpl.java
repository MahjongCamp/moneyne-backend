package org.mahjongcamp.moneynebackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mahjongcamp.moneynebackend.entity.User;
import org.mahjongcamp.moneynebackend.mapper.UserMapper;
import org.mahjongcamp.moneynebackend.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    Digester md5 = new Digester(DigestAlgorithm.MD5);

    @Override
    public User findUserByName(String username) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>();
        q.eq(User::getUsername,username);
        return getOne(q);
    }

    @Override
    public void sign(User user) {
        if (findUserByName(user.getUsername()) != null) {
            throw new RuntimeException("用户名重复");
        }
        String psw = md5.digestHex(user.getPassword());
        user.setPassword(psw);
        save(user);
        StpUtil.login(findUserByName(user.getUsername()).getId());
        StpUtil.getSession().set("user", user);
    }

    @Override
    public Boolean login(User user) {
        if (findUserByName(user.getUsername()) == null) {
            return false;
        }
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>();
        q.eq(User::getUsername,user.getUsername());
        User one = getOne(q);
        String psw = md5.digestHex(user.getPassword());
        if (!StrUtil.contains(psw,one.getPassword())){
            return false;
        }
        StpUtil.login(one.getId());
        StpUtil.getSession().set("user", user);
        return true;
    }
}
