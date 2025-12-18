package com.threadtutorial.collections;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

/**
 * ConcurrentLinkedQueue并发链表队列示例
 * 演示无界非阻塞线程安全队列
 */
@Slf4j
public class ConcurrentLinkedQueueDemo {

    public static void main(String[] args) throws Exception {
        log.info("=== ConcurrentLinkedQueue并发链表队列示例 ===");
        
        log.info("\n1. 基本操作示例:");
        basicOperationsExample();
        
        log.info("\n2. 线程安全测试:");
        threadSafetyExample();
        
        log.info("\n3. 生产者-消费者模式:");
        producerConsumerExample();
        
        log.info("\n4. 性能特点分析:");
        performanceAnalysisExample();
        
        log.info("\n5. 与BlockingQueue对比:");
        comparisonWithBlockingQueue();
    }
    
    /**
     * 基本操作示例
     */
    private static void basicOperationsExample() {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        
        log.info("1) 添加元素:");
        queue.add("任务1");
        queue.offer("任务2");
        queue.add("任务3");
        log.info("添加后队列:  {}", queue);
        log.info("队列大小:  {}", queue.size());
        
        log.info("\n2) 查看元素:");
        log.info("队首元素:  {}", queue.peek());
        log.info("查看后队列:  {}", queue);
        
        log.info("\n3) 移除元素:");
        log.info("移除元素:  {}", queue.poll());
        log.info("移除后队列:  {}", queue);
        
        log.info("\n4) 遍历元素:");
        System.out.print("队列元素: ");
        for (String item : queue) {
            System.out.print(item + " ");
        }
        log.info("");
        
        log.info("\n5) 其他操作:");
        log.info("是否为空:  {}", queue.isEmpty());
        log.info("是否包含'任务2':  {}", queue.contains("任务2"));
        queue.clear();
        log.info("清空后队列:  {}", queue);
    }
    
    /**
     * 线程安全测试
     */
    private static void threadSafetyExample() throws InterruptedException {
        log.info("测试ConcurrentLinkedQueue的线程安全性:");
        
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger addCount = new AtomicInteger(0);
        AtomicInteger removeCount = new AtomicInteger(0);
        
        // 初始化队列
        for (int i = 0; i < 10; i++) {
            queue.add(i);
        }
        
        // 创建添加线程（5个）
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < 20; j++) {
                    queue.add(threadId * 100 + j);
                    addCount.incrementAndGet();
                    try {
                        Thread.sleep(random.nextInt(10));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        // 创建移除线程（5个）
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < 20; j++) {
                    if (!queue.isEmpty()) {
                        queue.poll();
                        removeCount.incrementAndGet();
                    }
                    try {
                        Thread.sleep(random.nextInt(15));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        log.info("添加操作次数:  {}", addCount.get());
        log.info("移除操作次数:  {}", removeCount.get());
        log.info("最终队列大小:  {}", queue.size());
        log.info("测试完成，无并发异常");
    }
    
    /**
     * 生产者-消费者模式
     */
    private static void producerConsumerExample() throws InterruptedException {
        log.info("使用ConcurrentLinkedQueue实现生产者-消费者模式:");
        
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        
        // 多个生产者
        for (int i = 0; i < 2; i++) {
            final int producerId = i;
            executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < 15; j++) {
                    int item = random.nextInt(100);
                    queue.add(item);
                    produced.incrementAndGet();
                    log.info("生产者 {}", producerId + " 生产: " + item + 
                                     " (队列大小: " + queue.size() + ")");
                    try {
                        Thread.sleep(random.nextInt(100));
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
                for (int j = 0; j < 15; j++) {
                    Integer item = queue.poll();
                    if (item != null) {
                        consumed.incrementAndGet();
                        log.info("消费者 {}", consumerId + " 消费: " + item + 
                                         " (队列大小: " + queue.size() + ")");
                    }
                    try {
                        Thread.sleep(random.nextInt(150));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        log.info("\n生产总数:  {}", produced.get());
        log.info("消费总数:  {}", consumed.get());
        log.info("最终队列大小:  {}", queue.size());
        
        log.info("\nConcurrentLinkedQueue特点:");
        log.info("1. 无界队列，不会阻塞生产者");
        log.info("2. 非阻塞操作，poll()返回null如果队列为空");
        log.info("3. 适合高并发场景，吞吐量高");
        log.info("4. 迭代器是弱一致性的");
    }
    
    /**
     * 性能特点分析
     */
    private static void performanceAnalysisExample() {
        log.info("ConcurrentLinkedQueue性能特点分析:");
        
        log.info("\n1. 数据结构:");
        log.info("   - 基于链接节点的无界线程安全队列");
        log.info("   - 遵循FIFO（先进先出）原则");
        log.info("   - 使用CAS（Compare-And-Swap）操作实现线程安全");
        
        log.info("\n2. 操作特性:");
        log.info("   - add/offer: 添加到队列尾部，非阻塞");
        log.info("   - poll: 移除并返回队列头部元素，非阻塞");
        log.info("   - peek: 查看队列头部元素，不移除");
        log.info("   - size: 需要遍历整个队列，时间复杂度O(n)");
        
        log.info("\n3. 线程安全机制:");
        log.info("   - 使用CAS操作，无锁算法");
        log.info("   - 适合高并发读写的场景");
        log.info("   - 不会抛出ConcurrentModificationException");
        
        log.info("\n4. 适用场景:");
        log.info("   ✓ 高并发生产者-消费者场景");
        log.info("   ✓ 需要非阻塞操作的场景");
        log.info("   ✓ 队列大小变化频繁的场景");
        log.info("   ✓ 对吞吐量要求高的场景");
        
        log.info("\n5. 注意事项:");
        log.info("   ✗ size()方法需要遍历，性能较差");
        log.info("   ✗ 迭代器是弱一致性的");
        log.info("   ✗ 不支持阻塞操作");
    }
    
    /**
     * 与BlockingQueue对比
     */
    private static void comparisonWithBlockingQueue() {
        log.info("ConcurrentLinkedQueue vs BlockingQueue对比:");
        
        log.info("\n1. 阻塞特性:");
        log.info("   ConcurrentLinkedQueue: 非阻塞，操作立即返回");
        log.info("   BlockingQueue: 阻塞，队列空/满时会等待");
        
        log.info("\n2. 边界限制:");
        log.info("   ConcurrentLinkedQueue: 无界，不会满");
        log.info("   BlockingQueue: 可以有界或无界");
        
        log.info("\n3. 性能特点:");
        log.info("   ConcurrentLinkedQueue: 高吞吐量，CAS无锁");
        log.info("   BlockingQueue: 吞吐量较低，使用锁");
        
        log.info("\n4. 使用场景:");
        log.info("   ConcurrentLinkedQueue: 高并发，非阻塞场景");
        log.info("   BlockingQueue: 需要流量控制，生产者-消费者同步");
        
        log.info("\n5. 选择建议:");
        log.info("   使用ConcurrentLinkedQueue当:");
        log.info("     - 需要最高性能的并发队列");
        log.info("     - 可以接受poll()返回null");
        log.info("     - 不需要阻塞特性");
        log.info("     - 队列大小不重要或可能很大");
        
        log.info("\n   使用BlockingQueue当:");
        log.info("     - 需要生产者-消费者同步");
        log.info("     - 需要流量控制（有界队列）");
        log.info("     - 需要阻塞等待队列非空/非满");
        log.info("     - 需要优先级或延迟特性");
        
        log.info("\n6. 实际应用:");
        log.info("   ConcurrentLinkedQueue: 任务调度，事件处理");
        log.info("   BlockingQueue: 线程池任务队列，资源池");
    }
}
