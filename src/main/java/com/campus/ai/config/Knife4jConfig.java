package com.campus.ai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置 — 全局X-Token请求头
 * 登录后获取的Token填入页面上的X-Token输入框，所有接口自动携带
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园AI助手 API 文档")
                        .version("1.0.0")
                        .description("组员B RBAC权限控制\n\n登录后在\"请求头\"标签填入X-Token即可测试所有接口"));
    }

    /**
     * 给每个接口添加 X-Token 请求头参数
     * 在API文档页的每个接口上都能看到并填写
     */
    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            Parameter tokenHeader = new HeaderParameter()
                    .name("X-Token")
                    .description("登录后获取的Token")
                    .required(false);
            operation.addParametersItem(tokenHeader);
            return operation;
        };
    }
}
