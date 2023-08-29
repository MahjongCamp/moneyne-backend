package org.mahjongcamp.moneynebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mahjongcamp.moneynebackend.entity.User;

import java.security.GeneralSecurityException;

public interface UserService extends IService<User> {

    User findUserByName(String username);

    void sign(User user);

    Boolean login(User user);

    void sendVerifyCode(User user) throws GeneralSecurityException;

    void modifyPassword(User user);

    void modifyUsername(User user);

    void forgetPass(User user);
}
