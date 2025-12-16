package com.threadtutorial.basics;

/**
 * 线程创建方式示例
 * 演示三种创建线程的方式：
 * 1. 继承Thread类
 * 2. 实现Runnable接口
 * 3. 使用Lambda表达式
 */
public class ThreadCreation {

    public static void main(String[] args) {
        System.out.println("=== Java线程创建方式示例 ===");
        System.out.println("主线程名称: " + Thread.currentThread().getName());
        
        // 方式1：继承Thread类
        System.out.println("\n1. 继承Thread类创建线程:");
        MyThread thread1 = new MyThread("Thread-1");
        thread1.start();
        
        // 方式2：实现Runnable接口
        System.out.println("\n2. 实现Runnable接口创建线程:");
        Thread thread2 = new Thread(new MyRunnable(), "Thread-2");
        thread2.start();
        
        // 方式3：使用Lambda表达式（Java 8+）
        System.out.println("\n3. 使用Lambda表达式创建线程:");
        Thread thread3 = new Thread(() -> {
            System.out.println("线程 " + Thread.currentThread().getName() + " 正在运行 (Lambda)");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程 " + Thread.currentThread().getName() + " 执行完成 (Lambda)");
        }, "Thread-3");
        thread3.start();
        
        // 等待所有线程完成
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n所有线程执行完成！");
    }
    
    /**
     * 方式1：通过继承Thread类创建线程
     */
    static class MyThread extends Thread {
        public MyThread(String name) {
            super(name);
        }
        
        @Override
        public void run() {
            System.out.println("线程 " + getName() + " 正在运行 (继承Thread类)");
            try {
                // 模拟工作
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程 " + getName() + " 执行完成 (继承Thread类)");
        }
    }
    
    /**
     * 方式2：通过实现Runnable接口创建线程
     */
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("线程 " + Thread.currentThread().getName() + " 正在运行 (实现Runnable接口)");
            try {
                // 模拟工作
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程 " + Thread.currentThread().getName() + " 执行完成 (实现Runnable接口)");
        }
    }
}
