package com.lifeos;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LifeOSApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeOSApplication.class, args);
        System.out.println(" Successfully Executed.....!  ");
    }


}