package com.jack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@SpringBootConfiguration声明这是一个配置类+@EnableAutoConfiguration自动配置+@ComponentScan组件扫描
//public class LoginDemoApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(LoginDemoApplication.class, args);
//    }
//
//}
public class LoginDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoginDemoApplication.class, args);
    }
}