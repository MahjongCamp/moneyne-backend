package org.mahjongcamp.moneynebackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.extra.mail.Mail;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        //邮箱验证码比较
        String code = (String) StpUtil.getSession().getDataMap().get(user.getUsername());
        if (!code.equals(user.getVerifyCode())) {
            throw new RuntimeException("验证码不正确");
        }
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
        StpUtil.getSession().set(user.getUsername(), code);
    }
}
