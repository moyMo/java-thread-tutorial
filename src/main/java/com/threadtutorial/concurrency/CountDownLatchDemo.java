package com.threadtutorial.concurrency;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch示例
 * 演示如何使用CountDownLatch进行线程同步
 */
@Slf4j
public class CountDownLatchDemo {
    
    public static void main(String[] args) throws InterruptedException {
        log.info("=== CountDownLatch示例 ===");
        
        // 示例1：等待多个任务完成
        log.info("\n1. 等待多个任务完成:");
        testMultipleTasks();
        
        // 示例2：比赛开始信号
        log.info("\n2. 比赛开始信号:");
        testRaceStart();
        
        // 示例3：超时等待
        log.info("\n3. 超时等待:");
        testTimeout();
        
        log.info("\n所有示例执行完成！");
    }
    
    /**
     * 示例1：等待多个任务完成
     */
    private static void testMultipleTasks() throws InterruptedException {
        final int taskCount = 5;
        CountDownLatch latch = new CountDownLatch(taskCount);
        
        log.info("启动  {}", taskCount + " 个任务...");
        
        // 创建并启动多个工作线程
        for (int i = 1; i <= taskCount; i++) {
            final int taskId = i;
            new Thread(() -> {
                log.info("任务  {}", taskId + " 开始执行");
                try {
                    // 模拟任务执行时间
                    Thread.sleep(taskId * 1000);
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
                log.info("任务  {}", taskId + " 执行完成");
                latch.countDown(); // 任务完成，计数器减1
            }, "Task-" + i).start();
        }
        
        log.info("主线程等待所有任务完成...");
        latch.await(); // 等待所有任务完成
        log.info("所有任务已完成，继续执行主线程");
    }
    
    /**
     * 示例2：比赛开始信号
     */
    private static void testRaceStart() throws InterruptedException {
        final int runnerCount = 4;
        CountDownLatch readyLatch = new CountDownLatch(runnerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(runnerCount);
        
        log.info("跑步比赛准备开始，有  {}", runnerCount + " 名选手");
        
        // 创建跑步选手线程
        for (int i = 1; i <= runnerCount; i++) {
            final int runnerId = i;
            new Thread(() -> {
                try {
                    // 选手准备
                    log.info("选手  {}", runnerId + " 正在准备...");
                    Thread.sleep((long) (Math.random() * 2000));
                    log.info("选手  {}", runnerId + " 准备就绪");
                    readyLatch.countDown();
                    
                    // 等待开始信号
                    startLatch.await();
                    
                    // 开始跑步
                    log.info("选手  {}", runnerId + " 开始跑步！");
                    Thread.sleep((long) (Math.random() * 3000));
                    
                    // 到达终点
                    log.info("选手  {}", runnerId + " 到达终点！");
                    finishLatch.countDown();
                    
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            }, "Runner-" + i).start();
        }
        
        // 等待所有选手准备就绪
        readyLatch.await();
        log.info("\n所有选手准备就绪！");
        
        // 倒计时
        for (int i = 3; i > 0; i--) {
            log.info("{} ...", i );
            Thread.sleep(1000);
        }
        log.info("开始！\n");
        
        // 发出开始信号
        startLatch.countDown();
        
        // 等待所有选手到达终点
        finishLatch.await();
        log.info("比赛结束！");
    }
    
    /**
     * 示例3：超时等待
     */
    private static void testTimeout() {
        final int workerCount = 3;
        CountDownLatch latch = new CountDownLatch(workerCount);
        
        log.info("启动  {}", workerCount + " 个工作线程（其中一个会超时）");
        
        // 创建工作线程
        for (int i = 1; i <= workerCount; i++) {
            final int workerId = i;
            new Thread(() -> {
                try {
                    if (workerId == 2) {
                        // 第二个工作线程模拟超时
                        log.info("工作线程  {}", workerId + " 需要较长时间...");
                        Thread.sleep(5000);
                    } else {
                        log.info("工作线程  {}", workerId + " 正常执行");
                        Thread.sleep(2000);
                    }
                    log.info("工作线程  {}", workerId + " 完成");
                    latch.countDown();
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            }, "Worker-" + i).start();
        }
        
        try {
            // 等待所有工作线程完成，最多等待3秒
            boolean completed = latch.await(3, TimeUnit.SECONDS);
            if (completed) {
                log.info("所有工作线程在3秒内完成");
            } else {
                log.info("超时！部分工作线程未在3秒内完成");
                log.info("剩余计数:  {}", latch.getCount());
            }
        } catch (InterruptedException e) {
            log.error("异常", e);
        }
    }
    
    /**
     * 实际应用场景：初始化多个服务
     */
    static class ServiceInitializer {
        private final CountDownLatch initializationLatch;
        private final int serviceCount;
        
        public ServiceInitializer(int serviceCount) {
            this.serviceCount = serviceCount;
            this.initializationLatch = new CountDownLatch(serviceCount);
        }
        
        public void initializeServices() {
            log.info("开始初始化  {}", serviceCount + " 个服务...");
            
            for (int i = 0; i < serviceCount; i++) {
                final int serviceId = i;
                new Thread(() -> {
                    try {
                        log.info("服务  {}", serviceId + " 初始化中...");
                        Thread.sleep(1000 + (long) (Math.random() * 2000));
                        log.info("服务  {}", serviceId + " 初始化完成");
                        initializationLatch.countDown();
                    } catch (InterruptedException e) {
                        log.error("异常", e);
                    }
                }).start();
            }
        }
        
        public boolean waitForInitialization(long timeout, TimeUnit unit) throws InterruptedException {
            return initializationLatch.await(timeout, unit);
        }
    }
}
