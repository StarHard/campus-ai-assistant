package com.campus.ai.config;

/**
 * AI服务异常类
 *
 * @author A组长
 */
public class AiServiceException extends RuntimeException {

    private final Integer code;

    public AiServiceException(String message) {
        super(message);
        this.code = 502;
    }

    public AiServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = 502;
    }

    public Integer getCode() {
        return code;
    }
}
