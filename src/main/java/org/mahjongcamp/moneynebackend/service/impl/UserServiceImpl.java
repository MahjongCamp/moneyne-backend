package org.mahjongcamp.moneynebackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.extra.mail.Mail;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.mail.util.MailSSLSocketFactory;
import org.mahjongcamp.moneynebackend.entity.Email;
import org.mahjongcamp.moneynebackend.entity.User;
import org.mahjongcamp.moneynebackend.mapper.EmailMapper;
import org.mahjongcamp.moneynebackend.mapper.UserMapper;
import org.mahjongcamp.moneynebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private EmailMapper emailMapper;

    Digester md5 = new Digester(DigestAlgorithm.MD5);

    Cache<String, String> lfuCache = CacheUtil.newLFUCache(7);

    @Override
    public User findUserByEmail(String emailAdd) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>();
        q.eq(User::getEmailAdd,emailAdd);
        return getOne(q);
    }

    @Override
    public User sign(User user) {
        if (findUserByEmail(user.getEmailAdd()) != null) {
            throw new RuntimeException("邮箱重复");
        }
        String psw = md5.digestHex(user.getPassword());
        user.setPassword(psw);
        //邮箱验证码比较
//        String code = (String) StpUtil.getSession().getDataMap().get(user.getUsername());
        //从缓存中获取验证码数据
        String code = lfuCache.get(user.getEmailAdd());
        if (StrUtil.isBlank(code) || !code.equals(user.getVerifyCode())) {
            throw new RuntimeException("验证码不正确");
        }
        save(user);
        //是否限制该账号同端互斥登录
        String device = user.getDevice();
        if (StrUtil.isBlank(device)){
            StpUtil.login(findUserByEmail(user.getEmailAdd()).getId());
        }else {
            StpUtil.login(findUserByEmail(user.getEmailAdd()).getId(),device);
        }
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>();
        q.eq(User::getEmailAdd,user.getEmailAdd());
        User one = getOne(q);
        StpUtil.getSession().set("user", one);
        return one;
    }

    @Override
    public User login(User user) {
        if (findUserByEmail(user.getEmailAdd()) == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>();
        q.eq(User::getEmailAdd,user.getEmailAdd());
        User one = getOne(q);
        String psw = md5.digestHex(user.getPassword());
        if (!StrUtil.equals(psw,one.getPassword())){
            throw new RuntimeException("用户名或密码错误");
        }
        //是否限制该账号同端互斥登录
        String device = user.getDevice();
        if (StrUtil.isBlank(device)){
            StpUtil.login(one.getId());
        }else {
            StpUtil.login(one.getId(),device);
        }
        StpUtil.getSession().set("user", one);
        return one;
    }

    @Override
    public void sendVerifyCode(User user) throws GeneralSecurityException {
        //生成验证码
        String code = RandomUtil.randomNumbers(6);
        //设置发件邮箱
        LambdaQueryWrapper<Email> q = new LambdaQueryWrapper<>();
        q.eq(Email::getEmailType, "gmail");
        Email gmail = emailMapper.selectOne(q);
        MailAccount mailAccount = new MailAccount();
        mailAccount.setAuth(true);
        mailAccount.setSslEnable(true);
        mailAccount.setHost(gmail.getHost());
        mailAccount.setPort(gmail.getPort());
        mailAccount.setFrom(gmail.getFromAdd());
        mailAccount.setUser(gmail.getUser());
        mailAccount.setPass(gmail.getPass());

        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        mailAccount.setCustomProperty("mail.smtp.ssl.socketFactory", sf);

        Mail.create(mailAccount)
                .setTos(user.getEmailAdd())
                .setTitle("钱呢")
                .setContent("您的验证码是：" + code)
                .setHtml(true)
                .send();
        //将验证码存储到session中，用于等会比较
//        StpUtil.getSession().set(user.getUsername(), code);
        //存入缓存
        lfuCache.put(user.getEmailAdd(), code);
    }

    @Override
    public void modifyPassword(User user) {
        //当前登录用户
        StpUtil.getLoginId();
        User currentUser = new User().getCurrentUser();
        String psw = md5.digestHex(user.getPassword());
        if (!StrUtil.equals(psw,currentUser.getPassword())){
            throw new RuntimeException("密码错误");
        }
        if (StrUtil.isBlank(user.getNPass())){
            throw new RuntimeException("新密码不可为空");
        }
        currentUser.setPassword(md5.digestHex(user.getNPass()));
        updateById(currentUser);
    }

    @Override
    public void modifyUsername(User user) {
        //当前登录用户
        User currentUser = new User().getCurrentUser();
        if (StrUtil.isBlank(user.getUsername())){
            throw new RuntimeException("新用户名不可为空");
        }
        currentUser.setUsername(user.getUsername());
        updateById(currentUser);
    }

    @Override
    public void forgetPass(User user) {
        //验证码
        String code = lfuCache.get(user.getEmailAdd());
        if (StrUtil.isBlank(code) || !code.equals(user.getVerifyCode())) {
            throw new RuntimeException("验证码不正确");
        }
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        User one = getOne(q.eq(User::getEmailAdd,user.getEmailAdd()));
        one.setPassword(md5.digestHex(user.getNPass()));
        updateById(one);
    }

}
