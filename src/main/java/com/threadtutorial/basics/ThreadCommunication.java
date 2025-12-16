package com.threadtutorial.basics;

/**
 * 线程通信示例
 * 演示线程间的通信机制：wait(), notify(), notifyAll()
 * 以及使用BlockingQueue进行线程通信
 */
public class ThreadCommunication {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Java线程通信示例 ===");
        
        System.out.println("\n1. 使用wait()和notify()进行线程通信:");
        waitNotifyExample();
        
        System.out.println("\n2. 生产者-消费者模式示例:");
        producerConsumerExample();
        
        System.out.println("\n3. 使用notifyAll()唤醒所有等待线程:");
        notifyAllExample();
    }
    
    /**
     * wait()和notify()示例
     */
    private static void waitNotifyExample() throws InterruptedException {
        Message message = new Message();
        
        // 等待线程
        Thread waiter = new Thread(() -> {
            synchronized (message) {
                System.out.println("等待线程: 等待消息...");
                try {
                    message.wait(); // 释放锁并等待
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("等待线程: 收到消息: " + message.getContent());
            }
        }, "Waiter-Thread");
        
        // 通知线程
        Thread notifier = new Thread(() -> {
            synchronized (message) {
                try {
                    Thread.sleep(1000); // 模拟准备工作
                    message.setContent("Hello from Notifier!");
                    System.out.println("通知线程: 设置消息并通知等待线程");
                    message.notify(); // 唤醒一个等待线程
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Notifier-Thread");
        
        waiter.start();
        Thread.sleep(100); // 确保等待线程先启动
        notifier.start();
        
        waiter.join();
        notifier.join();
    }
    
    /**
     * 生产者-消费者模式示例
     */
    private static void producerConsumerExample() throws InterruptedException {
        SharedBuffer buffer = new SharedBuffer(3);
        
        // 生产者线程
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    buffer.produce("产品-" + i);
                    Thread.sleep(300); // 模拟生产时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("生产者: 生产完成");
        }, "Producer-Thread");
        
        // 消费者线程
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    String product = buffer.consume();
                    Thread.sleep(500); // 模拟消费时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("消费者: 消费完成");
        }, "Consumer-Thread");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
    }
    
    /**
     * notifyAll()示例 - 唤醒所有等待线程
     */
    private static void notifyAllExample() throws InterruptedException {
        Object lock = new Object();
        
        // 创建多个等待线程
        for (int i = 1; i <= 3; i++) {
            final int threadId = i;
            Thread waiter = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("等待线程-" + threadId + ": 开始等待");
                    try {
                        lock.wait();
                        System.out.println("等待线程-" + threadId + ": 被唤醒");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Waiter-" + threadId);
            waiter.start();
        }
        
        // 通知线程
        Thread notifier = new Thread(() -> {
            try {
                Thread.sleep(1000); // 让所有等待线程都进入等待状态
                synchronized (lock) {
                    System.out.println("\n通知线程: 调用notifyAll()唤醒所有等待线程");
                    lock.notifyAll(); // 唤醒所有等待线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Notifier-Thread");
        
        notifier.start();
        
        // 等待所有线程完成
        Thread.sleep(2000);
    }
    
    /**
     * 消息类
     */
    static class Message {
        private String content;
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 共享缓冲区类
     */
    static class SharedBuffer {
        private final String[] buffer;
        private int count = 0;
        private int putIndex = 0;
        private int takeIndex = 0;
        
        public SharedBuffer(int capacity) {
            buffer = new String[capacity];
        }
        
        public synchronized void produce(String product) throws InterruptedException {
            while (count == buffer.length) {
                System.out.println("缓冲区已满，生产者等待...");
                wait(); // 缓冲区满，等待
            }
            
            buffer[putIndex] = product;
            putIndex = (putIndex + 1) % buffer.length;
            count++;
            System.out.println("生产者生产: " + product + " (缓冲区大小: " + count + "/" + buffer.length + ")");
            
            notifyAll(); // 通知消费者可以消费了
        }
        
        public synchronized String consume() throws InterruptedException {
            while (count == 0) {
                System.out.println("缓冲区为空，消费者等待...");
                wait(); // 缓冲区空，等待
            }
            
            String product = buffer[takeIndex];
            takeIndex = (takeIndex + 1) % buffer.length;
            count--;
            System.out.println("消费者消费: " + product + " (缓冲区大小: " + count + "/" + buffer.length + ")");
            
            notifyAll(); // 通知生产者可以生产了
            return product;
        }
    }
}
