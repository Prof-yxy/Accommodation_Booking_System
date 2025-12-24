package com.camping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 露营预订系统主应用类
 */
@SpringBootApplication
@EnableTransactionManagement
public class CampingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampingApplication.class, args);
    }
}
