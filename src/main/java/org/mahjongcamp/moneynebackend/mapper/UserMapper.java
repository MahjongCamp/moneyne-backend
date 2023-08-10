package org.mahjongcamp.moneynebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mahjongcamp.moneynebackend.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
