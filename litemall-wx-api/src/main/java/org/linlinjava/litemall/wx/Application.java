package org.linlinjava.litemall.wx;

import org.linlinjava.litemall.pay.EnableLeShuaPay;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"org.linlinjava.litemall.db", "org.linlinjava.litemall.core", "org.linlinjava.litemall.wx"})
@MapperScan("org.linlinjava.litemall.db.dao")
@EnableTransactionManagement
@EnableScheduling
@EnableLeShuaPay
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}