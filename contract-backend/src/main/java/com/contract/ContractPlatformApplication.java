package com.contract;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.contract.mapper")
@EnableScheduling
public class ContractPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractPlatformApplication.class, args);
    }
}
