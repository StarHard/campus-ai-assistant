package com.campus.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 多线程并发控制配置
 * 用于AI请求处理和流式响应
 *
 * @author A组长
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    @Value("${concurrent.ai-thread-pool.core-pool-size:5}")
    private int aiCorePoolSize;

    @Value("${concurrent.ai-thread-pool.maximum-pool-size:20}")
    private int aiMaximumPoolSize;

    @Value("${concurrent.ai-thread-pool.queue-capacity:100}")
    private int aiQueueCapacity;

    @Value("${concurrent.streaming-thread-pool.core-pool-size:10}")
    private int streamingCorePoolSize;

    @Value("${concurrent.streaming-thread-pool.maximum-pool-size:50}")
    private int streamingMaximumPoolSize;

    @Value("${concurrent.streaming-thread-pool.queue-capacity:200}")
    private int streamingQueueCapacity;

    /**
     * AI请求处理线程池
     * 用于处理AI模型的同步和异步调用
     */
    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(aiCorePoolSize);
        executor.setMaximumPoolSize(aiMaximumPoolSize);
        executor.setQueueCapacity(aiQueueCapacity);
        executor.setThreadNamePrefix("AI-Worker-");
        // 拒绝策略：由调用线程执行（保证任务不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("AI线程池初始化完成: core={}, max={}, queue={}",
                aiCorePoolSize, aiMaximumPoolSize, aiQueueCapacity);
        return executor;
    }

    /**
     * 流式响应线程池
     * 用于处理大模型流式输出，防止前端界面卡顿
     */
    @Bean("streamingTaskExecutor")
    public Executor streamingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(streamingCorePoolSize);
        executor.setMaximumPoolSize(streamingMaximumPoolSize);
        executor.setQueueCapacity(streamingQueueCapacity);
        executor.setThreadNamePrefix("Streaming-Worker-");
        // 拒绝策略：丢弃最旧的任务（流式场景下，新数据更重要）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("流式响应线程池初始化完成: core={}, max={}, queue={}",
                streamingCorePoolSize, streamingMaximumPoolSize, streamingQueueCapacity);
        return executor;
    }
}
