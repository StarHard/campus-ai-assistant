package com.campus.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园智能服务小助手 - 应用启动类
 * 基于Spring Boot + SpringAI Alibaba构建
 *
 * @author A组长
 * @version 1.0.0
 */
@SpringBootApplication
public class CampusAiAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusAiAssistantApplication.class, args);
        System.out.println("===========================================");
        System.out.println("   校园智能服务小助手启动成功！");
        System.out.println("   访问地址: http://localhost:8080/api");
        System.out.println("   API文档: http://localhost:8080/api/doc.html");
        System.out.println("===========================================");
    }
}
