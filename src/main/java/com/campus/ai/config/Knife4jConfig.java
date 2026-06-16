package com.campus.ai.config;

import com.campus.ai.annotation.RequireRole;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

/**
 * Knife4j 配置 — 仅在需要登录的接口上显示 X-Token 请求头
 * 登录/注册等公开接口不会显示 X-Token 输入框
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园AI助手 API 文档")
                        .version("1.0.0")
                        .description("登录后点右上角 Authorize 填入Token，或直接在接口的 X-Token 输入框填写"));
    }

    /**
     * 只在标注了 @RequireRole 的接口上显示 X-Token 请求头
     * 公开接口（登录、注册等）不显示，避免困扰
     */
    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            Method method = ((HandlerMethod) handlerMethod).getMethod();
            // 检查方法级别和类级别的 @RequireRole 注解
            RequireRole requireRole = method.getAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = method.getDeclaringClass().getAnnotation(RequireRole.class);
            }
            // 只有标注了 @RequireRole 的接口才显示 X-Token 输入框
            if (requireRole != null) {
                Parameter tokenHeader = new HeaderParameter()
                        .name("X-Token")
                        .description("登录后获取的Token")
                        .required(true);
                operation.addParametersItem(tokenHeader);
            }
            return operation;
        };
    }
}
