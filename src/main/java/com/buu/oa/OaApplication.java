package com.buu.oa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OA协同办公平台启动类
 * 扫描 com.buu.oa.mapper 下的 MyBatis Mapper 接口
 * 开启定时任务支持（用于Redis ZSet延迟提醒扫描）
 */
@SpringBootApplication
@MapperScan("com.buu.oa.mapper")
@EnableScheduling
public class OaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaApplication.class, args);
    }

}
