package org.mahjongcamp.moneynebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mahjongcamp.moneynebackend.entity.User;

public interface UserService extends IService<User> {

    User findUserByName(String username);

    void sign(User user);

    Boolean login(User user);
}
