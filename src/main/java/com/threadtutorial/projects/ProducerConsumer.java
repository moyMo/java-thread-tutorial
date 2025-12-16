package com.threadtutorial.projects;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产者消费者模式示例
 * 演示多线程环境下的生产者消费者模式实现
 */
public class ProducerConsumer {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 生产者消费者模式示例 ===");
        
        // 示例1：使用BlockingQueue实现
        System.out.println("\n1. 使用BlockingQueue实现:");
        testBlockingQueueImplementation();
        
        // 示例2：自定义缓冲区实现
        System.out.println("\n2. 自定义缓冲区实现:");
        testCustomBufferImplementation();
        
        // 示例3：多生产者和多消费者
        System.out.println("\n3. 多生产者和多消费者:");
        testMultipleProducersConsumers();
        
        System.out.println("\n所有示例执行完成！");
    }
    
    /**
     * 示例1：使用BlockingQueue实现生产者消费者
     */
    private static void testBlockingQueueImplementation() throws InterruptedException {
        // 创建容量为5的阻塞队列
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        
        // 创建生产者
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    // 生产物品
                    System.out.println("生产者生产: " + i);
                    queue.put(i); // 如果队列满，会阻塞等待
                    producedCount.incrementAndGet();
                    Thread.sleep(200); // 模拟生产时间
                }
                // 生产结束信号
                queue.put(-1);
                System.out.println("生产者完成生产");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Producer");
        
        // 创建消费者
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    // 消费物品
                    Integer item = queue.take(); // 如果队列空，会阻塞等待
                    if (item == -1) {
                        // 收到结束信号，将信号放回队列供其他消费者使用
                        queue.put(-1);
                        break;
                    }
                    System.out.println("消费者消费: " + item);
                    consumedCount.incrementAndGet();
                    Thread.sleep(300); // 模拟消费时间
                }
                System.out.println("消费者完成消费");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Consumer");
        
        // 启动生产者和消费者
        producer.start();
        consumer.start();
        
        // 等待完成
        producer.join();
        consumer.join();
        
        System.out.println("生产总数: " + producedCount.get() + ", 消费总数: " + consumedCount.get());
    }
    
    /**
     * 示例2：自定义缓冲区实现生产者消费者
     */
    private static void testCustomBufferImplementation() throws InterruptedException {
        final int bufferSize = 3;
        CustomBuffer buffer = new CustomBuffer(bufferSize);
        
        // 创建生产者
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 8; i++) {
                    buffer.produce(i);
                    System.out.println("自定义生产者生产: " + i);
                    Thread.sleep(150);
                }
                buffer.produce(-1); // 结束信号
                System.out.println("自定义生产者完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "CustomProducer");
        
        // 创建消费者
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    int item = buffer.consume();
                    if (item == -1) {
                        buffer.produce(-1); // 将结束信号放回缓冲区
                        break;
                    }
                    System.out.println("自定义消费者消费: " + item);
                    Thread.sleep(250);
                }
                System.out.println("自定义消费者完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "CustomConsumer");
        
        // 启动线程
        producer.start();
        consumer.start();
        
        // 等待完成
        producer.join();
        consumer.join();
    }
    
    /**
     * 示例3：多生产者和多消费者
     */
    private static void testMultipleProducersConsumers() throws InterruptedException {
        final int producerCount = 3;
        final int consumerCount = 2;
        final int itemsPerProducer = 4;
        
        // 使用LinkedBlockingQueue作为共享缓冲区
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(5);
        AtomicInteger totalProduced = new AtomicInteger(0);
        AtomicInteger totalConsumed = new AtomicInteger(0);
        
        System.out.println("启动 " + producerCount + " 个生产者和 " + consumerCount + " 个消费者");
        System.out.println("每个生产者生产 " + itemsPerProducer + " 个物品");
        
        // 创建生产者线程
        Thread[] producers = new Thread[producerCount];
        for (int i = 0; i < producerCount; i++) {
            final int producerId = i + 1;
            producers[i] = new Thread(() -> {
                try {
                    for (int j = 1; j <= itemsPerProducer; j++) {
                        String item = "P" + producerId + "-Item" + j;
                        queue.put(item);
                        totalProduced.incrementAndGet();
                        System.out.println("生产者 " + producerId + " 生产: " + item);
                        Thread.sleep((long) (Math.random() * 300));
                    }
                    System.out.println("生产者 " + producerId + " 完成");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Producer-" + producerId);
        }
        
        // 创建消费者线程
        Thread[] consumers = new Thread[consumerCount];
        for (int i = 0; i < consumerCount; i++) {
            final int consumerId = i + 1;
            consumers[i] = new Thread(() -> {
                try {
                    int consumed = 0;
                    while (consumed < (producerCount * itemsPerProducer) / consumerCount) {
                        String item = queue.take();
                        totalConsumed.incrementAndGet();
                        consumed++;
                        System.out.println("消费者 " + consumerId + " 消费: " + item);
                        Thread.sleep((long) (Math.random() * 400));
                    }
                    System.out.println("消费者 " + consumerId + " 完成，消费了 " + consumed + " 个物品");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Consumer-" + consumerId);
        }
        
        // 启动所有线程
        for (Thread producer : producers) {
            producer.start();
        }
        for (Thread consumer : consumers) {
            consumer.start();
        }
        
        // 等待所有生产者完成
        for (Thread producer : producers) {
            producer.join();
        }
        
        // 添加结束信号（每个消费者一个）
        for (int i = 0; i < consumerCount; i++) {
            queue.put("END");
        }
        
        // 等待所有消费者完成
        for (Thread consumer : consumers) {
            consumer.join();
        }
        
        System.out.println("总计生产: " + totalProduced.get() + ", 总计消费: " + totalConsumed.get());
    }
    
    /**
     * 自定义缓冲区实现
     */
    static class CustomBuffer {
        private final int[] buffer;
        private int count = 0;
        private int putIndex = 0;
        private int takeIndex = 0;
        
        public CustomBuffer(int capacity) {
            this.buffer = new int[capacity];
        }
        
        public synchronized void produce(int item) throws InterruptedException {
            while (count == buffer.length) {
                // 缓冲区满，等待
                System.out.println("缓冲区满，生产者等待...");
                wait();
            }
            
            buffer[putIndex] = item;
            putIndex = (putIndex + 1) % buffer.length;
            count++;
            
            System.out.println("生产后缓冲区大小: " + count + "/" + buffer.length);
            
            // 通知消费者
            notifyAll();
        }
        
        public synchronized int consume() throws InterruptedException {
            while (count == 0) {
                // 缓冲区空，等待
                System.out.println("缓冲区空，消费者等待...");
                wait();
            }
            
            int item = buffer[takeIndex];
            takeIndex = (takeIndex + 1) % buffer.length;
            count--;
            
            System.out.println("消费后缓冲区大小: " + count + "/" + buffer.length);
            
            // 通知生产者
            notifyAll();
            
            return item;
        }
        
        public synchronized int getSize() {
            return count;
        }
        
        public synchronized int getCapacity() {
            return buffer.length;
        }
    }
    
    /**
     * 实际应用：日志处理系统
     */
    static class LogProcessingSystem {
        private final BlockingQueue<String> logQueue;
        private final Thread[] processors;
        private volatile boolean running = true;
        
        public LogProcessingSystem(int processorCount, int queueSize) {
            this.logQueue = new ArrayBlockingQueue<>(queueSize);
            this.processors = new Thread[processorCount];
            
            // 初始化处理器线程
            for (int i = 0; i < processorCount; i++) {
                final int processorId = i + 1;
                processors[i] = new Thread(() -> {
                    while (running || !logQueue.isEmpty()) {
                        try {
                            String log = logQueue.poll(100, TimeUnit.MILLISECONDS);
                            if (log != null) {
                                processLog(processorId, log);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    System.out.println("日志处理器 " + processorId + " 停止");
                }, "LogProcessor-" + processorId);
            }
        }
        
        public void start() {
            for (Thread processor : processors) {
                processor.start();
            }
            System.out.println("日志处理系统启动，有 " + processors.length + " 个处理器");
        }
        
        public void stop() {
            running = false;
            for (Thread processor : processors) {
                processor.interrupt();
            }
        }
        
        public void submitLog(String log) throws InterruptedException {
            logQueue.put(log);
        }
        
        private void processLog(int processorId, String log) {
            // 模拟日志处理
            System.out.println("处理器 " + processorId + " 处理日志: " + log);
            try {
                Thread.sleep((long) (Math.random() * 200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
