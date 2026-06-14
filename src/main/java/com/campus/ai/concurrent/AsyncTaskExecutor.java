package com.campus.ai.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 异步任务执行器
 * 封装线程池操作，提供统一的异步任务执行接口
 * 用于处理AI请求和流式响应的异步调用
 *
 * @author A组长
 */
@Component
public class AsyncTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskExecutor.class);

    private final Executor aiTaskExecutor;
    private final Executor streamingTaskExecutor;

    public AsyncTaskExecutor(
            @Qualifier("aiTaskExecutor") Executor aiTaskExecutor,
            @Qualifier("streamingTaskExecutor") Executor streamingTaskExecutor) {
        this.aiTaskExecutor = aiTaskExecutor;
        this.streamingTaskExecutor = streamingTaskExecutor;
    }

    /**
     * 执行AI相关异步任务（使用AI专用线程池）
     *
     * @param task 任务
     * @return Future对象，可用于获取任务结果
     */
    public <T> CompletableFuture<T> executeAiTask(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                log.error("AI任务执行失败", e);
                throw new CompletionException(e);
            }
        }, aiTaskExecutor);
    }

    /**
     * 执行AI相关异步任务（无返回值）
     *
     * @param task 任务
     */
    public void executeAiTask(Runnable task) {
        CompletableFuture.runAsync(task, aiTaskExecutor);
    }

    /**
     * 执行流式响应异步任务（使用流式专用线程池）
     *
     * @param task 任务
     * @return Future对象
     */
    public <T> CompletableFuture<T> executeStreamingTask(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                log.error("流式任务执行失败", e);
                throw new CompletionException(e);
            }
        }, streamingTaskExecutor);
    }

    /**
     * 执行流式响应异步任务（无返回值）
     *
     * @param task 任务
     */
    public void executeStreamingTask(Runnable task) {
        CompletableFuture.runAsync(task, streamingTaskExecutor);
    }

    /**
     * 带超时的异步任务执行
     *
     * @param task 任务
     * @param timeout 超时时间（毫秒）
     * @param defaultValue 超时后的默认返回值
     * @return 任务结果或默认值
     */
    public <T> T executeWithTimeout(Callable<T> task, long timeout, T defaultValue) {
        try {
            Future<T> future = ((ExecutorService) aiTaskExecutor).submit(task);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("任务执行超时: {}ms", timeout);
            return defaultValue;
        } catch (Exception e) {
            log.error("任务执行异常", e);
            return defaultValue;
        }
    }
}
