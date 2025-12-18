package com.threadtutorial.basics;

import lombok.extern.slf4j.Slf4j;

/**
 * 线程创建方式示例
 * 演示三种创建线程的方式：
 * 1. 继承Thread类
 * 2. 实现Runnable接口
 * 3. 使用Lambda表达式
 */
@Slf4j
public class ThreadCreation {

    public static void main(String[] args) {
        log.info("=== Java线程创建方式示例 ===");
        log.info("主线程名称: {}", Thread.currentThread().getName());
        
        // 方式1：继承Thread类
        log.info("\n1. 继承Thread类创建线程:");
        MyThread thread1 = new MyThread("Thread-1");
        thread1.start();
        
        // 方式2：实现Runnable接口
        log.info("\n2. 实现Runnable接口创建线程:");
        Thread thread2 = new Thread(new MyRunnable(), "Thread-2");
        thread2.start();
        
        // 方式3：使用Lambda表达式（Java 8+）
        log.info("\n3. 使用Lambda表达式创建线程:");
        Thread thread3 = new Thread(() -> {
            log.info("线程 {} 正在运行 (Lambda)", Thread.currentThread().getName());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("Lambda线程被中断", e);
            }
            log.info("线程 {} 执行完成 (Lambda)", Thread.currentThread().getName());
        }, "Thread-3");
        thread3.start();
        
        // 等待所有线程完成
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            log.error("主线程被中断", e);
        }
        
        log.info("\n所有线程执行完成！");
    }
    
    /**
     * 方式1：通过继承Thread类创建线程
     */
    @Slf4j
    static class MyThread extends Thread {
        public MyThread(String name) {
            super(name);
        }
        
        @Override
        public void run() {
            log.info("线程 {} 正在运行 (继承Thread类)", getName());
            try {
                // 模拟工作
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("MyThread线程被中断", e);
            }
            log.info("线程 {} 执行完成 (继承Thread类)", getName());
        }
    }
    
    /**
     * 方式2：通过实现Runnable接口创建线程
     */
    @Slf4j
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            log.info("线程 {} 正在运行 (实现Runnable接口)", Thread.currentThread().getName());
            try {
                // 模拟工作
                Thread.sleep(800);
            } catch (InterruptedException e) {
                log.error("MyRunnable线程被中断", e);
            }
            log.info("线程 {} 执行完成 (实现Runnable接口)", Thread.currentThread().getName());
        }
    }
}
