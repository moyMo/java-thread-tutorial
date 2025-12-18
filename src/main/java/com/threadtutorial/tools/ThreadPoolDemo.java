package com.threadtutorial.tools;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池使用示例
 * 演示不同类型的线程池及其配置
 */
@Slf4j
public class ThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {
        log.info("=== Java线程池使用示例 ===");
        
        log.info("\n1. 固定大小线程池:");
        fixedThreadPoolExample();
        
        log.info("\n2. 缓存线程池:");
        cachedThreadPoolExample();
        
        log.info("\n3. 单线程线程池:");
        singleThreadPoolExample();
        
        log.info("\n4. 定时线程池:");
        scheduledThreadPoolExample();
        
        log.info("\n5. 自定义线程池:");
        customThreadPoolExample();
    }
    
    /**
     * 固定大小线程池示例
     */
    private static void fixedThreadPoolExample() throws InterruptedException {
        log.info("创建固定大小线程池 (4个线程)");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 提交10个任务
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                log.info("任务  {}", taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(500); // 模拟任务执行
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
                log.info("任务  {}", taskId + " 完成");
            });
        }
        
        // 优雅关闭
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        log.info("固定大小线程池示例完成");
    }
    
    /**
     * 缓存线程池示例
     */
    private static void cachedThreadPoolExample() throws InterruptedException {
        log.info("创建缓存线程池 (根据需要创建新线程)");
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // 提交20个任务
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                log.info("任务  {}", taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(200); // 模拟任务执行
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            });
        }
        
        // 显示线程池状态
        Thread.sleep(1000);
        log.info("缓存线程池示例完成");
        
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }
    
    /**
     * 单线程线程池示例
     */
    private static void singleThreadPoolExample() throws InterruptedException {
        log.info("创建单线程线程池 (保证任务顺序执行)");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // 提交5个任务
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                log.info("任务  {}", taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(300); // 模拟任务执行
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
        log.info("单线程线程池示例完成");
    }
    
    /**
     * 定时线程池示例
     */
    private static void scheduledThreadPoolExample() throws InterruptedException {
        log.info("创建定时线程池 (支持定时和周期性任务)");
        var scheduler = Executors.newScheduledThreadPool(2);
        
        log.info("1) 延迟执行任务:");
        scheduler.schedule(() -> {
            log.info("延迟任务执行:  {}", Thread.currentThread().getName());
        }, 1, TimeUnit.SECONDS);
        
        log.info("2) 固定速率周期性任务:");
        scheduler.scheduleAtFixedRate(() -> {
            log.info("固定速率任务执行:  {}", Thread.currentThread().getName());
        }, 2, 1, TimeUnit.SECONDS);
        
        log.info("3) 固定延迟周期性任务:");
        scheduler.scheduleWithFixedDelay(() -> {
            log.info("固定延迟任务执行:  {}", Thread.currentThread().getName());
            try {
                Thread.sleep(500); // 模拟任务执行时间
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
        }, 3, 1, TimeUnit.SECONDS);
        
        // 运行一段时间后关闭
        Thread.sleep(5000);
        scheduler.shutdown();
        log.info("定时线程池示例完成");
    }
    
    /**
     * 自定义线程池示例
     */
    private static void customThreadPoolExample() throws InterruptedException {
        log.info("创建自定义线程池:");
        log.info("- 核心线程数: 2");
        log.info("- 最大线程数: 4");
        log.info("- 队列容量: 10");
        log.info("- 线程空闲时间: 30秒");
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, // 核心线程数
            4, // 最大线程数
            30, TimeUnit.SECONDS, // 线程空闲时间
            new ArrayBlockingQueue<>(10), // 工作队列
            new CustomThreadFactory(), // 自定义线程工厂
            new CustomRejectionHandler() // 自定义拒绝策略
        );
        
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
        
        // 提交15个任务
        for (int i = 1; i <= 15; i++) {
            final int taskId = i;
            try {
                executor.submit(() -> {
                    log.info("任务  {}", taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                    try {
                        Thread.sleep(400); // 模拟任务执行
                    } catch (InterruptedException e) {
                        log.error("异常", e);
                    }
                });
            } catch (Exception e) {
                log.info("任务  {}", taskId + " 被拒绝: " + e.getMessage());
            }
        }
        
        // 监控线程池状态
        monitorThreadPool(executor);
        
        // 优雅关闭
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        log.info("自定义线程池示例完成");
    }
    
    /**
     * 监控线程池状态
     */
    private static void monitorThreadPool(ThreadPoolExecutor executor) throws InterruptedException {
        log.info("\n线程池监控:");
        log.info("核心线程数:  {}", executor.getCorePoolSize());
        log.info("最大线程数:  {}", executor.getMaximumPoolSize());
        log.info("当前线程数:  {}", executor.getPoolSize());
        log.info("活跃线程数:  {}", executor.getActiveCount());
        log.info("已完成任务数:  {}", executor.getCompletedTaskCount());
        log.info("总任务数:  {}", executor.getTaskCount());
        log.info("队列大小:  {}", executor.getQueue().size());
        
        Thread.sleep(2000);
        
        log.info("\n2秒后线程池状态:");
        log.info("当前线程数:  {}", executor.getPoolSize());
        log.info("活跃线程数:  {}", executor.getActiveCount());
        log.info("已完成任务数:  {}", executor.getCompletedTaskCount());
    }
    
    /**
     * 自定义线程工厂
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "CustomThread-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
    
    /**
     * 自定义拒绝策略
     */
    static class CustomRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.info("任务被拒绝，当前队列大小:  {}", executor.getQueue().size());
            // 可以在这里实现自定义逻辑，如记录日志、重试等
            throw new RuntimeException("线程池已满，任务被拒绝");
        }
    }
}
