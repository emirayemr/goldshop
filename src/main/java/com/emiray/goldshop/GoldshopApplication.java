package com.emiray.goldshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GoldshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoldshopApplication.class, args);
    }

}
