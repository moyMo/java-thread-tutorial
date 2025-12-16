package com.threadtutorial.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch示例
 * 演示如何使用CountDownLatch进行线程同步
 */
public class CountDownLatchDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CountDownLatch示例 ===");
        
        // 示例1：等待多个任务完成
        System.out.println("\n1. 等待多个任务完成:");
        testMultipleTasks();
        
        // 示例2：比赛开始信号
        System.out.println("\n2. 比赛开始信号:");
        testRaceStart();
        
        // 示例3：超时等待
        System.out.println("\n3. 超时等待:");
        testTimeout();
        
        System.out.println("\n所有示例执行完成！");
    }
    
    /**
     * 示例1：等待多个任务完成
     */
    private static void testMultipleTasks() throws InterruptedException {
        final int taskCount = 5;
        CountDownLatch latch = new CountDownLatch(taskCount);
        
        System.out.println("启动 " + taskCount + " 个任务...");
        
        // 创建并启动多个工作线程
        for (int i = 1; i <= taskCount; i++) {
            final int taskId = i;
            new Thread(() -> {
                System.out.println("任务 " + taskId + " 开始执行");
                try {
                    // 模拟任务执行时间
                    Thread.sleep(taskId * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("任务 " + taskId + " 执行完成");
                latch.countDown(); // 任务完成，计数器减1
            }, "Task-" + i).start();
        }
        
        System.out.println("主线程等待所有任务完成...");
        latch.await(); // 等待所有任务完成
        System.out.println("所有任务已完成，继续执行主线程");
    }
    
    /**
     * 示例2：比赛开始信号
     */
    private static void testRaceStart() throws InterruptedException {
        final int runnerCount = 4;
        CountDownLatch readyLatch = new CountDownLatch(runnerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(runnerCount);
        
        System.out.println("跑步比赛准备开始，有 " + runnerCount + " 名选手");
        
        // 创建跑步选手线程
        for (int i = 1; i <= runnerCount; i++) {
            final int runnerId = i;
            new Thread(() -> {
                try {
                    // 选手准备
                    System.out.println("选手 " + runnerId + " 正在准备...");
                    Thread.sleep((long) (Math.random() * 2000));
                    System.out.println("选手 " + runnerId + " 准备就绪");
                    readyLatch.countDown();
                    
                    // 等待开始信号
                    startLatch.await();
                    
                    // 开始跑步
                    System.out.println("选手 " + runnerId + " 开始跑步！");
                    Thread.sleep((long) (Math.random() * 3000));
                    
                    // 到达终点
                    System.out.println("选手 " + runnerId + " 到达终点！");
                    finishLatch.countDown();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Runner-" + i).start();
        }
        
        // 等待所有选手准备就绪
        readyLatch.await();
        System.out.println("\n所有选手准备就绪！");
        
        // 倒计时
        for (int i = 3; i > 0; i--) {
            System.out.println(i + "...");
            Thread.sleep(1000);
        }
        System.out.println("开始！\n");
        
        // 发出开始信号
        startLatch.countDown();
        
        // 等待所有选手到达终点
        finishLatch.await();
        System.out.println("比赛结束！");
    }
    
    /**
     * 示例3：超时等待
     */
    private static void testTimeout() {
        final int workerCount = 3;
        CountDownLatch latch = new CountDownLatch(workerCount);
        
        System.out.println("启动 " + workerCount + " 个工作线程（其中一个会超时）");
        
        // 创建工作线程
        for (int i = 1; i <= workerCount; i++) {
            final int workerId = i;
            new Thread(() -> {
                try {
                    if (workerId == 2) {
                        // 第二个工作线程模拟超时
                        System.out.println("工作线程 " + workerId + " 需要较长时间...");
                        Thread.sleep(5000);
                    } else {
                        System.out.println("工作线程 " + workerId + " 正常执行");
                        Thread.sleep(2000);
                    }
                    System.out.println("工作线程 " + workerId + " 完成");
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Worker-" + i).start();
        }
        
        try {
            // 等待所有工作线程完成，最多等待3秒
            boolean completed = latch.await(3, TimeUnit.SECONDS);
            if (completed) {
                System.out.println("所有工作线程在3秒内完成");
            } else {
                System.out.println("超时！部分工作线程未在3秒内完成");
                System.out.println("剩余计数: " + latch.getCount());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            System.out.println("开始初始化 " + serviceCount + " 个服务...");
            
            for (int i = 0; i < serviceCount; i++) {
                final int serviceId = i;
                new Thread(() -> {
                    try {
                        System.out.println("服务 " + serviceId + " 初始化中...");
                        Thread.sleep(1000 + (long) (Math.random() * 2000));
                        System.out.println("服务 " + serviceId + " 初始化完成");
                        initializationLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        
        public boolean waitForInitialization(long timeout, TimeUnit unit) throws InterruptedException {
            return initializationLatch.await(timeout, unit);
        }
    }
}
