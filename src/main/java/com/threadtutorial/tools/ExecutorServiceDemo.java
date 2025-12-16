package com.threadtutorial.tools;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ExecutorService示例
 * 演示Java线程池的使用
 */
public class ExecutorServiceDemo {
    
    public static void main(String[] args) {
        System.out.println("=== ExecutorService线程池示例 ===");
        
        // 示例1：固定大小线程池
        System.out.println("\n1. 固定大小线程池 (FixedThreadPool):");
        testFixedThreadPool();
        
        // 示例2：缓存线程池
        System.out.println("\n2. 缓存线程池 (CachedThreadPool):");
        testCachedThreadPool();
        
        // 示例3：定时线程池
        System.out.println("\n3. 定时线程池 (ScheduledThreadPool):");
        testScheduledThreadPool();
        
        // 示例4：自定义线程池
        System.out.println("\n4. 自定义线程池 (ThreadPoolExecutor):");
        testCustomThreadPool();
        
        System.out.println("\n所有示例执行完成！");
    }
    
    /**
     * 固定大小线程池示例
     */
    private static void testFixedThreadPool() {
        // 创建固定大小为4的线程池
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 创建10个任务
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("任务 " + taskId + " 正在执行，线程: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(500); // 模拟任务执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("任务 " + taskId + " 执行完成");
            });
        }
        
        // 关闭线程池
        executor.shutdown();
        try {
            // 等待所有任务完成，最多等待5秒
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("部分任务未在5秒内完成，强制关闭");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    /**
     * 缓存线程池示例
     */
    private static void testCachedThreadPool() {
        // 创建缓存线程池
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // 创建任务计数器
        AtomicInteger completedTasks = new AtomicInteger(0);
        
        // 创建20个任务，每个任务执行时间不同
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("缓存池任务 " + taskId + " 开始，线程: " + Thread.currentThread().getName());
                try {
                    // 模拟不同执行时间
                    Thread.sleep(taskId * 100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                completedTasks.incrementAndGet();
                System.out.println("缓存池任务 " + taskId + " 完成，已完成: " + completedTasks.get());
            });
        }
        
        // 关闭线程池
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    /**
     * 定时线程池示例
     */
    private static void testScheduledThreadPool() {
        // 创建定时线程池
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        
        System.out.println("开始定时任务演示...");
        
        // 1. 延迟执行
        scheduler.schedule(() -> {
            System.out.println("延迟5秒执行的任务");
        }, 5, TimeUnit.SECONDS);
        
        // 2. 固定频率执行
        ScheduledFuture<?> periodicTask = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("固定频率任务执行，时间: " + System.currentTimeMillis());
        }, 1, 2, TimeUnit.SECONDS);
        
        // 3. 固定延迟执行
        scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("固定延迟任务开始");
            try {
                Thread.sleep(1000); // 模拟任务执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("固定延迟任务结束");
        }, 2, 3, TimeUnit.SECONDS);
        
        // 10秒后取消固定频率任务并关闭调度器
        scheduler.schedule(() -> {
            System.out.println("取消固定频率任务");
            periodicTask.cancel(false);
            
            System.out.println("关闭调度器");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }, 10, TimeUnit.SECONDS);
    }
    
    /**
     * 自定义线程池示例
     */
    private static void testCustomThreadPool() {
        // 自定义线程池参数
        int corePoolSize = 2;      // 核心线程数
        int maxPoolSize = 4;       // 最大线程数
        long keepAliveTime = 30;   // 空闲线程存活时间
        int queueCapacity = 10;    // 任务队列容量
        
        // 创建自定义线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(queueCapacity),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "CustomThread-" + threadNumber.getAndIncrement());
                    thread.setDaemon(false);
                    thread.setPriority(Thread.NORM_PRIORITY);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程执行
        );
        
        System.out.println("自定义线程池状态:");
        System.out.println("核心线程数: " + corePoolSize);
        System.out.println("最大线程数: " + maxPoolSize);
        System.out.println("队列容量: " + queueCapacity);
        
        // 提交15个任务（超过队列容量，会触发拒绝策略）
        for (int i = 1; i <= 15; i++) {
            final int taskId = i;
            try {
                executor.submit(() -> {
                    System.out.println("自定义任务 " + taskId + " 执行，线程: " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                System.out.println("任务 " + taskId + " 已提交");
            } catch (Exception e) {
                System.out.println("任务 " + taskId + " 提交失败: " + e.getMessage());
            }
        }
        
        // 监控线程池状态
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.scheduleAtFixedRate(() -> {
            System.out.println("线程池状态 - 活跃线程: " + executor.getActiveCount() + 
                             ", 队列大小: " + executor.getQueue().size() +
                             ", 完成任务: " + executor.getCompletedTaskCount());
        }, 0, 1, TimeUnit.SECONDS);
        
        // 关闭线程池
        executor.shutdown();
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        // 关闭监控
        monitor.shutdown();
    }
}
