package com.threadtutorial.collections;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BlockingQueue阻塞队列示例
 * 演示各种阻塞队列的实现和使用场景
 */
public class BlockingQueueDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== BlockingQueue阻塞队列示例 ===");
        
        System.out.println("\n1. ArrayBlockingQueue示例:");
        arrayBlockingQueueExample();
        
        System.out.println("\n2. LinkedBlockingQueue示例:");
        linkedBlockingQueueExample();
        
        System.out.println("\n3. PriorityBlockingQueue示例:");
        priorityBlockingQueueExample();
        
        System.out.println("\n4. SynchronousQueue示例:");
        synchronousQueueExample();
        
        System.out.println("\n5. DelayQueue示例:");
        delayQueueExample();
        
        System.out.println("\n6. 生产者-消费者模式:");
        producerConsumerExample();
        
        System.out.println("\n7. 阻塞队列对比总结:");
        blockingQueueComparison();
    }
    
    /**
     * ArrayBlockingQueue示例
     */
    private static void arrayBlockingQueueExample() throws InterruptedException {
        System.out.println("ArrayBlockingQueue - 有界阻塞队列（数组实现）:");
        
        // 创建容量为3的ArrayBlockingQueue
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(3);
        
        System.out.println("队列容量: " + queue.remainingCapacity() + "/3");
        
        // 添加元素
        System.out.println("\n1. 添加元素:");
        queue.put("任务1");
        queue.put("任务2");
        queue.put("任务3");
        System.out.println("添加3个元素后: " + queue);
        System.out.println("队列大小: " + queue.size());
        
        // 尝试添加更多元素（会阻塞）
        System.out.println("\n2. 尝试添加第4个元素（队列已满）:");
        Thread adder = new Thread(() -> {
            try {
                System.out.println("尝试添加'任务4'...");
                queue.put("任务4"); // 会阻塞直到有空间
                System.out.println("成功添加'任务4'");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        adder.start();
        
        Thread.sleep(1000); // 让添加线程阻塞
        
        // 移除元素，释放空间
        System.out.println("移除一个元素: " + queue.take());
        adder.join(); // 等待添加线程完成
        
        System.out.println("当前队列: " + queue);
        
        // 使用offer和poll（非阻塞）
        System.out.println("\n3. 非阻塞操作:");
        boolean offered = queue.offer("任务5");
        System.out.println("offer('任务5'): " + offered + " (队列: " + queue + ")");
        
        String polled = queue.poll();
        System.out.println("poll(): " + polled + " (队列: " + queue + ")");
        
        // 带超时的操作
        System.out.println("\n4. 带超时的操作:");
        boolean offeredWithTimeout = queue.offer("任务6", 1, TimeUnit.SECONDS);
        System.out.println("offer('任务6', 1秒): " + offeredWithTimeout);
        
        String polledWithTimeout = queue.poll(1, TimeUnit.SECONDS);
        System.out.println("poll(1秒): " + polledWithTimeout);
    }
    
    /**
     * LinkedBlockingQueue示例
     */
    private static void linkedBlockingQueueExample() throws InterruptedException {
        System.out.println("LinkedBlockingQueue - 可选有界阻塞队列（链表实现）:");
        
        // 创建无界LinkedBlockingQueue
        BlockingQueue<Integer> unboundedQueue = new LinkedBlockingQueue<>();
        System.out.println("1. 无界队列（默认）:");
        System.out.println("初始容量: " + unboundedQueue.remainingCapacity() + " (Integer.MAX_VALUE)");
        
        // 添加大量元素
        for (int i = 0; i < 5; i++) {
            unboundedQueue.put(i);
        }
        System.out.println("添加5个元素后大小: " + unboundedQueue.size());
        
        // 创建有界LinkedBlockingQueue
        BlockingQueue<Integer> boundedQueue = new LinkedBlockingQueue<>(3);
        System.out.println("\n2. 有界队列（容量3）:");
        
        // 生产者-消费者演示
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        
        // 生产者
        executor.submit(() -> {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                try {
                    int item = random.nextInt(100);
                    boundedQueue.put(item);
                    produced.incrementAndGet();
                    System.out.println("生产: " + item + " (队列大小: " + boundedQueue.size() + ")");
                    Thread.sleep(random.nextInt(200));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // 消费者
        executor.submit(() -> {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                try {
                    Integer item = boundedQueue.take();
                    consumed.incrementAndGet();
                    System.out.println("消费: " + item + " (队列大小: " + boundedQueue.size() + ")");
                    Thread.sleep(random.nextInt(300));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\n生产总数: " + produced.get());
        System.out.println("消费总数: " + consumed.get());
        System.out.println("最终队列大小: " + boundedQueue.size());
    }
    
    /**
     * PriorityBlockingQueue示例
     */
    private static void priorityBlockingQueueExample() throws InterruptedException {
        System.out.println("PriorityBlockingQueue - 无界优先级阻塞队列:");
        
        // 创建优先级队列（按任务优先级排序）
        BlockingQueue<Task> queue = new PriorityBlockingQueue<>();
        
        System.out.println("1. 添加不同优先级的任务:");
        queue.put(new Task("低优先级任务", 3));
        queue.put(new Task("高优先级任务", 1));
        queue.put(new Task("中优先级任务", 2));
        queue.put(new Task("紧急任务", 0));
        
        System.out.println("队列大小: " + queue.size());
        
        System.out.println("\n2. 按优先级消费任务:");
        while (!queue.isEmpty()) {
            Task task = queue.take();
            System.out.println("消费: " + task);
        }
        
        // 多线程优先级队列示例
        System.out.println("\n3. 多线程优先级队列:");
        BlockingQueue<Integer> priorityQueue = new PriorityBlockingQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // 多个生产者添加随机优先级数据
        for (int i = 0; i < 3; i++) {
            final int producerId = i;
            executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < 5; j++) {
                    try {
                        int value = random.nextInt(100);
                        priorityQueue.put(value);
                        System.out.println("生产者" + producerId + " 添加: " + value);
                        Thread.sleep(random.nextInt(100));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        // 消费者按优先级消费
        executor.submit(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    Integer value = priorityQueue.take();
                    System.out.println("消费者 获取: " + value + " (队列大小: " + priorityQueue.size() + ")");
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
    }
    
    /**
     * SynchronousQueue示例
     */
    private static void synchronousQueueExample() throws InterruptedException {
        System.out.println("SynchronousQueue - 同步队列（容量为0）:");
        System.out.println("特点：每个插入操作必须等待另一个线程的移除操作");
        
        BlockingQueue<String> queue = new SynchronousQueue<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // 生产者
        executor.submit(() -> {
            try {
                System.out.println("生产者: 准备生产'数据A'...");
                queue.put("数据A");
                System.out.println("生产者: '数据A'已交付");
                
                Thread.sleep(500);
                
                System.out.println("生产者: 准备生产'数据B'...");
                queue.put("数据B");
                System.out.println("生产者: '数据B'已交付");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 消费者
        executor.submit(() -> {
            try {
                Thread.sleep(100); // 让生产者先开始
                
                System.out.println("消费者: 等待数据...");
                String data1 = queue.take();
                System.out.println("消费者: 收到'" + data1 + "'");
                
                Thread.sleep(1000);
                
                System.out.println("消费者: 等待更多数据...");
                String data2 = queue.take();
                System.out.println("消费者: 收到'" + data2 + "'");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
        
        System.out.println("\nSynchronousQueue使用场景:");
        System.out.println("- 线程间直接传递数据，无中间存储");
        System.out.println("- 确保生产者和消费者同步");
        System.out.println("- 用于工作窃取算法等场景");
    }
    
    /**
     * DelayQueue示例
     */
    private static void delayQueueExample() throws InterruptedException {
        System.out.println("DelayQueue - 延迟队列（元素在延迟期满后才能被取出）:");
        
        DelayQueue<DelayedTask> queue = new DelayQueue<>();
        
        System.out.println("1. 添加延迟任务:");
        long now = System.currentTimeMillis();
        queue.put(new DelayedTask("任务1", now + 2000)); // 2秒后到期
        queue.put(new DelayedTask("任务2", now + 1000)); // 1秒后到期
        queue.put(new DelayedTask("任务3", now + 3000)); // 3秒后到期
        
        System.out.println("队列大小: " + queue.size());
        System.out.println("当前时间: " + now);
        
        System.out.println("\n2. 按延迟时间消费任务:");
        for (int i = 0; i < 3; i++) {
            DelayedTask task = queue.take();
            long currentTime = System.currentTimeMillis();
            System.out.println("消费: " + task + " (当前时间: " + currentTime + 
                             ", 延迟: " + (currentTime - now) + "ms)");
        }
        
        // 定时任务调度示例
        System.out.println("\n3. 定时任务调度示例:");
        DelayQueue<DelayedTask> scheduler = new DelayQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // 添加定时任务
        long startTime = System.currentTimeMillis();
        scheduler.put(new DelayedTask("定时任务A", startTime + 1500));
        scheduler.put(new DelayedTask("定时任务B", startTime + 500));
        scheduler.put(new DelayedTask("定时任务C", startTime + 2500));
        
        // 任务执行器
        executor.submit(() -> {
            try {
                while (!scheduler.isEmpty()) {
                    DelayedTask task = scheduler.take();
                    long currentTime = System.currentTimeMillis();
                    System.out.println("执行" + task + " (实际延迟: " + 
                                     (currentTime - startTime) + "ms)");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        executor.shutdown();
        executor.awaitTermination(4, TimeUnit.SECONDS);
    }
    
    /**
     * 生产者-消费者模式
     */
    private static void producerConsumerExample() throws InterruptedException {
        System.out.println("使用BlockingQueue实现生产者-消费者模式:");
        
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        
        // 多个生产者
        for (int i = 0; i < 2; i++) {
            final int producerId = i;
            executor.submit(() -> {
                Random random = new Random();
                while (produced.get() < 20) {
                    try {
                        int item = random.nextInt(100);
                        queue.put(item);
                        produced.incrementAndGet();
                        System.out.println("生产者" + producerId + " 生产: " + item + 
                                         " (队列: " + queue.size() + "/5)");
                        Thread.sleep(random.nextInt(200));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        // 多个消费者
        for (int i = 0; i < 2; i++) {
            final int consumerId = i;
            executor.submit(() -> {
                Random random = new Random();
                while (consumed.get() < 20) {
                    try {
                        Integer item = queue.take();
                        consumed.incrementAndGet();
                        System.out.println("消费者" + consumerId + " 消费: " + item + 
                                         " (队列: " + queue.size() + "/5)");
                        Thread.sleep(random.nextInt(300));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("\n生产总数: " + produced.get());
        System.out.println("消费总数: " + consumed.get());
        System.out.println("最终队列大小: " + queue.size());
        
        System.out.println("\n生产者-消费者模式优势:");
        System.out.println("1. 解耦生产者和消费者");
        System.out.println("2. 平衡生产和消费速度");
        System.out.println("3. 支持异步处理");
        System.out.println("4. 提高系统吞吐量");
    }
    
    /**
     * 阻塞队列对比总结
     */
    private static void blockingQueueComparison() {
        System.out.println("阻塞队列类型对比总结:");
        
        System.out.println("\n1. ArrayBlockingQueue:");
        System.out.println("   - 实现: 数组");
        System.out.println("   - 容量: 有界");
        System.out.println("   - 排序: FIFO");
        System.out.println("   - 特点: 固定大小，性能稳定");
        System.out.println("   - 适用: 固定大小的线程池任务队列");
        
        System.out.println("\n2. LinkedBlockingQueue:");
        System.out.println("   - 实现: 链表");
        System.out.println("   - 容量: 可选有界（默认无界）");
        System.out.println("   - 排序: FIFO");
        System.out.println("   - 特点: 吞吐量高，可选边界");
        System.out.println("   - 适用: 通用生产者-消费者场景");
        
        System.out.println("\n3. PriorityBlockingQueue:");
        System.out.println("   - 实现: 堆");
        System.out.println("   - 容量: 无界");
        System.out.println("   - 排序: 按优先级");
        System.out.println("   - 特点: 元素必须实现Comparable");
        System.out.println("   - 适用: 任务调度，优先级处理");
        
        System.out.println("\n4. SynchronousQueue:");
        System.out.println("   - 实现: 无存储");
        System.out.println("   - 容量: 0");
        System.out.println("   - 排序: 无");
        System.out.println("   - 特点: 直接传递，无缓冲");
        System.out.println("   - 适用: 线程间直接传递，工作窃取");
        
        System.out.println("\n5. DelayQueue:");
        System.out.println("   - 实现: 优先级队列");
        System.out.println("   - 容量: 无界");
        System.out.println("   - 排序: 按延迟时间");
        System.out.println("   - 特点: 元素必须实现Delayed");
        System.out.println("   - 适用: 定时任务，缓存过期");
        
        System.out.println("\n选择建议:");
        System.out.println("1. 需要固定大小缓冲 -> ArrayBlockingQueue");
        System.out.println("2. 需要高吞吐量 -> LinkedBlockingQueue");
        System.out.println("3. 需要优先级处理 -> PriorityBlockingQueue");
        System.out.println("4. 需要直接传递 -> SynchronousQueue");
        System.out.println("5. 需要延迟执行 -> DelayQueue");
    }
}
