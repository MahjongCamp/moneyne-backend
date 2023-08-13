package org.mahjongcamp.moneynebackend;

import cn.dev33.satoken.SaManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoneyneBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyneBackendApplication.class, args);
        System.out.println("启动成功，Sa-Token 配置如下：" + SaManager.getConfig());
        System.out.println("测试");
    }


}
