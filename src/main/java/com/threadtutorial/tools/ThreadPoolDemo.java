package com.threadtutorial.tools;

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
public class ThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Java线程池使用示例 ===");
        
        System.out.println("\n1. 固定大小线程池:");
        fixedThreadPoolExample();
        
        System.out.println("\n2. 缓存线程池:");
        cachedThreadPoolExample();
        
        System.out.println("\n3. 单线程线程池:");
        singleThreadPoolExample();
        
        System.out.println("\n4. 定时线程池:");
        scheduledThreadPoolExample();
        
        System.out.println("\n5. 自定义线程池:");
        customThreadPoolExample();
    }
    
    /**
     * 固定大小线程池示例
     */
    private static void fixedThreadPoolExample() throws InterruptedException {
        System.out.println("创建固定大小线程池 (4个线程)");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 提交10个任务
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("任务 " + taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(500); // 模拟任务执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("任务 " + taskId + " 完成");
            });
        }
        
        // 优雅关闭
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("固定大小线程池示例完成");
    }
    
    /**
     * 缓存线程池示例
     */
    private static void cachedThreadPoolExample() throws InterruptedException {
        System.out.println("创建缓存线程池 (根据需要创建新线程)");
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // 提交20个任务
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("任务 " + taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(200); // 模拟任务执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        // 显示线程池状态
        Thread.sleep(1000);
        System.out.println("缓存线程池示例完成");
        
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }
    
    /**
     * 单线程线程池示例
     */
    private static void singleThreadPoolExample() throws InterruptedException {
        System.out.println("创建单线程线程池 (保证任务顺序执行)");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // 提交5个任务
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("任务 " + taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(300); // 模拟任务执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
        System.out.println("单线程线程池示例完成");
    }
    
    /**
     * 定时线程池示例
     */
    private static void scheduledThreadPoolExample() throws InterruptedException {
        System.out.println("创建定时线程池 (支持定时和周期性任务)");
        var scheduler = Executors.newScheduledThreadPool(2);
        
        System.out.println("1) 延迟执行任务:");
        scheduler.schedule(() -> {
            System.out.println("延迟任务执行: " + Thread.currentThread().getName());
        }, 1, TimeUnit.SECONDS);
        
        System.out.println("2) 固定速率周期性任务:");
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("固定速率任务执行: " + Thread.currentThread().getName());
        }, 2, 1, TimeUnit.SECONDS);
        
        System.out.println("3) 固定延迟周期性任务:");
        scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("固定延迟任务执行: " + Thread.currentThread().getName());
            try {
                Thread.sleep(500); // 模拟任务执行时间
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 3, 1, TimeUnit.SECONDS);
        
        // 运行一段时间后关闭
        Thread.sleep(5000);
        scheduler.shutdown();
        System.out.println("定时线程池示例完成");
    }
    
    /**
     * 自定义线程池示例
     */
    private static void customThreadPoolExample() throws InterruptedException {
        System.out.println("创建自定义线程池:");
        System.out.println("- 核心线程数: 2");
        System.out.println("- 最大线程数: 4");
        System.out.println("- 队列容量: 10");
        System.out.println("- 线程空闲时间: 30秒");
        
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
                    System.out.println("任务 " + taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                    try {
                        Thread.sleep(400); // 模拟任务执行
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.out.println("任务 " + taskId + " 被拒绝: " + e.getMessage());
            }
        }
        
        // 监控线程池状态
        monitorThreadPool(executor);
        
        // 优雅关闭
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("自定义线程池示例完成");
    }
    
    /**
     * 监控线程池状态
     */
    private static void monitorThreadPool(ThreadPoolExecutor executor) throws InterruptedException {
        System.out.println("\n线程池监控:");
        System.out.println("核心线程数: " + executor.getCorePoolSize());
        System.out.println("最大线程数: " + executor.getMaximumPoolSize());
        System.out.println("当前线程数: " + executor.getPoolSize());
        System.out.println("活跃线程数: " + executor.getActiveCount());
        System.out.println("已完成任务数: " + executor.getCompletedTaskCount());
        System.out.println("总任务数: " + executor.getTaskCount());
        System.out.println("队列大小: " + executor.getQueue().size());
        
        Thread.sleep(2000);
        
        System.out.println("\n2秒后线程池状态:");
        System.out.println("当前线程数: " + executor.getPoolSize());
        System.out.println("活跃线程数: " + executor.getActiveCount());
        System.out.println("已完成任务数: " + executor.getCompletedTaskCount());
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
            System.out.println("任务被拒绝，当前队列大小: " + executor.getQueue().size());
            // 可以在这里实现自定义逻辑，如记录日志、重试等
            throw new RuntimeException("线程池已满，任务被拒绝");
        }
    }
}
