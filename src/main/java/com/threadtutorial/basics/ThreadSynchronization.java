package com.threadtutorial.basics;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程同步示例
 * 演示三种线程同步方式：
 * 1. synchronized关键字
 * 2. ReentrantLock锁
 * 3. 原子操作（简单示例）
 */
public class ThreadSynchronization {
    
    private static int counter = 0;
    private static final Object lock = new Object();
    private static final Lock reentrantLock = new ReentrantLock();
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 线程同步示例 ===");
        System.out.println("初始计数器值: " + counter);
        
        // 测试synchronized方式
        testSynchronized();
        
        // 重置计数器
        counter = 0;
        Thread.sleep(1000);
        
        // 测试ReentrantLock方式
        testReentrantLock();
        
        System.out.println("\n所有测试完成！");
    }
    
    /**
     * 使用synchronized关键字进行同步
     */
    private static void testSynchronized() throws InterruptedException {
        System.out.println("\n1. 使用synchronized关键字同步:");
        
        // 创建10个线程，每个线程增加计数器1000次
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    synchronized (lock) {
                        counter++;
                    }
                }
            }, "Sync-Thread-" + i);
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("预期值: 10000, 实际值: " + counter);
        System.out.println("synchronized测试 " + (counter == 10000 ? "成功" : "失败"));
    }
    
    /**
     * 使用ReentrantLock进行同步
     */
    private static void testReentrantLock() throws InterruptedException {
        System.out.println("\n2. 使用ReentrantLock同步:");
        
        // 创建10个线程，每个线程增加计数器1000次
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    reentrantLock.lock();
                    try {
                        counter++;
                    } finally {
                        reentrantLock.unlock();
                    }
                }
            }, "Lock-Thread-" + i);
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("预期值: 10000, 实际值: " + counter);
        System.out.println("ReentrantLock测试 " + (counter == 10000 ? "成功" : "失败"));
    }
    
    /**
     * 演示synchronized方法的用法
     */
    static class SynchronizedCounter {
        private int count = 0;
        
        public synchronized void increment() {
            count++;
        }
        
        public synchronized int getCount() {
            return count;
        }
    }
    
    /**
     * 演示Lock接口的tryLock用法
     */
    static class TryLockExample {
        private final Lock lock = new ReentrantLock();
        
        public void performTask() {
            // 尝试获取锁，最多等待1秒
            boolean acquired = false;
            try {
                acquired = lock.tryLock();
                if (acquired) {
                    // 执行需要同步的操作
                    System.out.println(Thread.currentThread().getName() + " 获取到锁，执行任务");
                    Thread.sleep(500);
                } else {
                    System.out.println(Thread.currentThread().getName() + " 未能获取到锁，执行其他操作");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (acquired) {
                    lock.unlock();
                }
            }
        }
    }
}
