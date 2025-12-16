package com.threadtutorial.basics;

/**
 * 线程生命周期示例
 * 演示线程的各个状态：NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
 */
public class ThreadLifecycle {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Java线程生命周期示例 ===");
        
        // 创建线程但未启动 - NEW状态
        Thread thread = new Thread(new Worker(), "Worker-Thread");
        System.out.println("线程创建后状态: " + thread.getState()); // NEW
        
        // 启动线程 - 进入RUNNABLE状态
        thread.start();
        System.out.println("线程启动后状态: " + thread.getState()); // RUNNABLE
        
        // 主线程休眠一小会儿，让工作线程运行
        Thread.sleep(50);
        System.out.println("线程运行中状态: " + thread.getState()); // TIMED_WAITING (因为Worker中有sleep)
        
        // 等待线程完成 - TERMINATED状态
        thread.join();
        System.out.println("线程完成后状态: " + thread.getState()); // TERMINATED
        
        System.out.println("\n=== 演示BLOCKED状态 ===");
        demonstrateBlockedState();
        
        System.out.println("\n=== 演示WAITING状态 ===");
        demonstrateWaitingState();
    }
    
    /**
     * 工作线程类
     */
    static class Worker implements Runnable {
        @Override
        public void run() {
            System.out.println("工作线程开始执行: " + Thread.currentThread().getName());
            
            try {
                // 进入TIMED_WAITING状态
                System.out.println("工作线程进入休眠 (TIMED_WAITING)");
                Thread.sleep(1000);
                
                // 执行一些计算
                System.out.println("工作线程执行计算任务");
                for (int i = 0; i < 5; i++) {
                    System.out.println("计算: " + i);
                    Thread.sleep(100);
                }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            System.out.println("工作线程执行完成: " + Thread.currentThread().getName());
        }
    }
    
    /**
     * 演示BLOCKED状态
     */
    private static void demonstrateBlockedState() throws InterruptedException {
        Object lock = new Object();
        
        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("线程1获取到锁，进入同步块");
                try {
                    Thread.sleep(1000); // 持有锁一段时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("线程1释放锁");
            }
        }, "Thread-1");
        
        Thread thread2 = new Thread(() -> {
            System.out.println("线程2尝试获取锁...");
            synchronized (lock) {
                System.out.println("线程2获取到锁");
            }
        }, "Thread-2");
        
        thread1.start();
        Thread.sleep(100); // 确保thread1先启动
        thread2.start();
        
        Thread.sleep(200); // 给thread2时间尝试获取锁
        System.out.println("线程2状态 (等待锁时): " + thread2.getState()); // BLOCKED
        
        thread1.join();
        thread2.join();
    }
    
    /**
     * 演示WAITING状态
     */
    private static void demonstrateWaitingState() throws InterruptedException {
        Object lock = new Object();
        
        Thread waitingThread = new Thread(() -> {
            synchronized (lock) {
                System.out.println("等待线程获取到锁，调用wait()");
                try {
                    lock.wait(); // 进入WAITING状态
                    System.out.println("等待线程被唤醒");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Waiting-Thread");
        
        Thread notifyingThread = new Thread(() -> {
            try {
                Thread.sleep(500); // 让等待线程先进入等待状态
                synchronized (lock) {
                    System.out.println("通知线程获取到锁，调用notify()");
                    lock.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Notifying-Thread");
        
        waitingThread.start();
        notifyingThread.start();
        
        Thread.sleep(200); // 给等待线程时间进入wait()
        System.out.println("等待线程状态 (调用wait()后): " + waitingThread.getState()); // WAITING
        
        waitingThread.join();
        notifyingThread.join();
    }
}
