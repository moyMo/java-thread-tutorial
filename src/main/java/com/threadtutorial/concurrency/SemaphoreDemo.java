package com.threadtutorial.concurrency;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Semaphore（信号量）示例
 * 演示如何使用Semaphore控制并发访问资源的线程数量
 */
@Slf4j
public class SemaphoreDemo {
    
    public static void main(String[] args) throws InterruptedException {
        log.info("=== Semaphore（信号量）示例 ===");
        
        // 示例1：控制资源访问数量
        log.info("\n1. 控制资源访问数量（如数据库连接池）:");
        testResourcePool();
        
        // 示例2：生产者-消费者模式
        log.info("\n2. 生产者-消费者模式:");
        testProducerConsumer();
        
        // 示例3：限流控制
        log.info("\n3. 限流控制（API调用限制）:");
        testRateLimiting();
        
        // 示例4：公平与非公平信号量
        log.info("\n4. 公平与非公平信号量对比:");
        testFairVsNonFair();
        
        log.info("\n所有示例执行完成！");
    }
    
    /**
     * 示例1：控制资源访问数量（如数据库连接池）
     */
    private static void testResourcePool() throws InterruptedException {
        // 模拟一个只有3个连接的数据库连接池
        final int poolSize = 3;
        final int threadCount = 10;
        
        Semaphore connectionPool = new Semaphore(poolSize);
        AtomicInteger activeConnections = new AtomicInteger(0);
        
        log.info("数据库连接池大小:  {}", poolSize);
        log.info("模拟  {}", threadCount + " 个线程请求连接");
        
        // 创建多个线程尝试获取数据库连接
        Thread[] threads = new Thread[threadCount];
        for (int i = 1; i <= threadCount; i++) {
            final int threadId = i;
            threads[i-1] = new Thread(() -> {
                try {
                    log.info("线程  {}", threadId + " 尝试获取数据库连接...");
                    
                    // 获取许可（连接）
                    connectionPool.acquire();
                    int currentActive = activeConnections.incrementAndGet();
                    
                    log.info("线程  {}", threadId + " 获取到连接，当前活跃连接: " + currentActive);
                    
                    // 模拟数据库操作
                    Thread.sleep(1000 + (long) (Math.random() * 2000));
                    
                    // 释放连接
                    connectionPool.release();
                    currentActive = activeConnections.decrementAndGet();
                    
                    log.info("线程  {}", threadId + " 释放连接，当前活跃连接: " + currentActive);
                    
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            }, "DB-Thread-" + i);
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        log.info("所有数据库操作完成");
    }
    
    /**
     * 示例2：生产者-消费者模式
     */
    private static void testProducerConsumer() throws InterruptedException {
        // 缓冲区大小为5
        final int bufferSize = 5;
        final int itemCount = 20;
        
        // 信号量：empty表示空槽位数量，full表示已填充槽位数量
        Semaphore emptySlots = new Semaphore(bufferSize);  // 初始有5个空槽位
        Semaphore fullSlots = new Semaphore(0);            // 初始没有已填充的槽位
        Semaphore mutex = new Semaphore(1);                // 互斥锁，保护缓冲区访问
        
        int[] buffer = new int[bufferSize];
        AtomicInteger bufferIndex = new AtomicInteger(0);
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        
        log.info("生产者-消费者模式演示");
        log.info("缓冲区大小:  {}", bufferSize + ", 总生产项目: " + itemCount);
        
        // 生产者线程
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= itemCount; i++) {
                try {
                    // 等待空槽位
                    emptySlots.acquire();
                    
                    // 获取互斥锁访问缓冲区
                    mutex.acquire();
                    
                    // 生产项目
                    int index = bufferIndex.getAndIncrement() % bufferSize;
                    buffer[index] = i;
                    int produced = producedCount.incrementAndGet();
                    
                    log.info("生产者: 生产项目  {}", i + " 到缓冲区位置 " + index + 
                                     " (已生产: " + produced + ")");
                    
                    // 释放互斥锁
                    mutex.release();
                    
                    // 通知消费者有新的项目
                    fullSlots.release();
                    
                    // 模拟生产时间
                    Thread.sleep((long) (Math.random() * 300));
                    
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            }
            log.info("生产者完成所有生产任务");
        }, "Producer");
        
        // 消费者线程
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= itemCount; i++) {
                try {
                    // 等待有填充的槽位
                    fullSlots.acquire();
                    
                    // 获取互斥锁访问缓冲区
                    mutex.acquire();
                    
                    // 消费项目
                    int index = (bufferIndex.get() - 1) % bufferSize;
                    if (index < 0) index += bufferSize;
                    int item = buffer[index];
                    int consumed = consumedCount.incrementAndGet();
                    
                    log.info("消费者: 消费项目  {}", item + " 从缓冲区位置 " + index + 
                                     " (已消费: " + consumed + ")");
                    
                    // 释放互斥锁
                    mutex.release();
                    
                    // 通知生产者有空槽位
                    emptySlots.release();
                    
                    // 模拟消费时间
                    Thread.sleep((long) (Math.random() * 500));
                    
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            }
            log.info("消费者完成所有消费任务");
        }, "Consumer");
        
        // 启动生产者和消费者
        producer.start();
        consumer.start();
        
        // 等待完成
        producer.join();
        consumer.join();
        
        log.info("生产者-消费者模式演示完成");
        log.info("总计生产:  {}", producedCount.get() + ", 总计消费: " + consumedCount.get());
    }
    
    /**
     * 示例3：限流控制（API调用限制）
     */
    private static void testRateLimiting() throws InterruptedException {
        // 模拟API限流：每秒最多5个请求
        final int maxRequestsPerSecond = 5;
        final int totalRequests = 15;
        
        Semaphore rateLimiter = new Semaphore(maxRequestsPerSecond);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);
        
        log.info("API限流控制演示");
        log.info("限流:  {}", maxRequestsPerSecond + " 请求/秒");
        log.info("模拟  {}", totalRequests + " 个并发请求");
        
        // 创建多个请求线程
        Thread[] requestThreads = new Thread[totalRequests];
        for (int i = 1; i <= totalRequests; i++) {
            final int requestId = i;
            requestThreads[i-1] = new Thread(() -> {
                try {
                    log.info("请求  {}", requestId + ": 尝试调用API...");
                    
                    // 尝试获取许可，最多等待500毫秒
                    boolean acquired = rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS);
                    
                    if (acquired) {
                        // 模拟API调用
                        log.info("请求  {}", requestId + ": API调用成功");
                        Thread.sleep(200); // 模拟API处理时间
                        
                        // 释放许可（模拟限流窗口滑动）
                        // 在实际限流中，通常使用定时器释放许可
                        // 这里简化处理，立即释放
                        rateLimiter.release();
                        
                        successfulRequests.incrementAndGet();
                    } else {
                        log.info("请求  {}", requestId + ": API调用被限流（超时）");
                        failedRequests.incrementAndGet();
                    }
                    
                } catch (InterruptedException e) {
                    log.error("异常", e);
                }
            }, "API-Request-" + i);
        }
        
        // 启动所有请求线程
        for (Thread thread : requestThreads) {
            thread.start();
            // 稍微错开启动时间，模拟真实请求
            Thread.sleep(50);
        }
        
        // 等待所有请求完成
        for (Thread thread : requestThreads) {
            thread.join();
        }
        
        log.info("\n限流结果统计:");
        log.info("成功请求:  {}", successfulRequests.get());
        log.info("失败请求（被限流）:  {}", failedRequests.get());
        log.info("成功率:  {}", String.format("%.1f%%", (successfulRequests.get() * 100.0 / totalRequests)));
    }
    
    /**
     * 示例4：公平与非公平信号量对比
     */
    private static void testFairVsNonFair() throws InterruptedException {
        final int threadCount = 5;
        final int acquireAttempts = 3;
        
        log.info("公平与非公平信号量对比演示");
        log.info("线程数:  {}", threadCount + ", 每个线程尝试获取许可 " + acquireAttempts + " 次");
        
        // 测试非公平信号量
        log.info("\nA. 非公平信号量:");
        testSemaphore(false, threadCount, acquireAttempts);
        
        Thread.sleep(2000); // 间隔一下
        
        // 测试公平信号量
        log.info("\nB. 公平信号量:");
        testSemaphore(true, threadCount, acquireAttempts);
        
        log.info("\n对比总结:");
        log.info("- 非公平信号量: 性能更高，但可能导致线程饥饿");
        log.info("- 公平信号量: 保证先请求的线程先获取许可，性能稍低但更公平");
    }
    
    /**
     * 测试特定类型的信号量
     */
    private static void testSemaphore(boolean fair, int threadCount, int acquireAttempts) 
            throws InterruptedException {
        // 创建信号量（只有1个许可）
        Semaphore semaphore = new Semaphore(1, fair);
        String semaphoreType = fair ? "公平" : "非公平";
        
        log.info("使用 {}", semaphoreType + "信号量，初始许可: 1");
        
        // 创建线程并记录获取顺序
        Thread[] threads = new Thread[threadCount];
        int[] acquisitionOrder = new int[threadCount];
        AtomicInteger orderIndex = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int attempt = 1; attempt <= acquireAttempts; attempt++) {
                    try {
                        // 尝试获取许可
                        semaphore.acquire();
                        
                        // 记录获取顺序
                        int order = orderIndex.incrementAndGet();
                        acquisitionOrder[threadId] = order;
                        
                        log.info("线程  {}", threadId + " 第" + attempt + "次获取到许可" +
                                         " (总顺序: " + order + ")");
                        
                        // 持有许可一段时间
                        Thread.sleep(100);
                        
                        // 释放许可
                        semaphore.release();
                        
                        // 等待一下再尝试下一次
                        Thread.sleep(50);
                        
                    } catch (InterruptedException e) {
                        log.error("异常", e);
                    }
                }
            }, semaphoreType + "-Thread-" + i);
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 分析获取模式
        log.info("\n {}", semaphoreType + "信号量获取模式分析:");
        boolean isFairPattern = true;
        for (int i = 0; i < threadCount; i++) {
            if (acquisitionOrder[i] != i + 1) {
                isFairPattern = false;
                break;
            }
        }
        
        if (fair) {
            log.info("公平信号量 {}", (isFairPattern ? "按请求顺序分配许可" : "未完全按顺序分配"));
        } else {
            log.info("非公平信号量 {}", (isFairPattern ? "意外按顺序分配" : "未按顺序分配"));
        }
    }
    
    /**
     * 实际应用场景：连接池管理器
     */
    static class ConnectionPoolManager {
        private final Semaphore availableConnections;
        private final int maxConnections;
        private final boolean[] connectionInUse;
        
        public ConnectionPoolManager(int maxConnections) {
            this.maxConnections = maxConnections;
            this.availableConnections = new Semaphore(maxConnections, true); // 公平信号量
            this.connectionInUse = new boolean[maxConnections];
        }
        
        public int acquireConnection() throws InterruptedException {
            availableConnections.acquire();
            return assignConnection();
        }
        
        public boolean tryAcquireConnection(long timeout, TimeUnit unit) throws InterruptedException {
            if (availableConnections.tryAcquire(timeout, unit)) {
                assignConnection();
                return true;
            }
            return false;
        }
        
        private synchronized int assignConnection() {
            for (int i = 0; i < maxConnections; i++) {
                if (!connectionInUse[i]) {
                    connectionInUse[i] = true;
                    log.info("分配连接  {}", i + "，剩余可用连接: " + availableConnections.availablePermits());
                    return i;
                }
            }
            // 正常情况下不会执行到这里
            return -1;
        }
        
        public synchronized void releaseConnection(int connectionId) {
            if (connectionId >= 0 && connectionId < maxConnections && connectionInUse[connectionId]) {
                connectionInUse[connectionId] = false;
                availableConnections.release();
                log.info("释放连接  {}", connectionId + "，剩余可用连接: " + (availableConnections.availablePermits() + 1));
            }
        }
        
        public int getAvailableConnections() {
            return availableConnections.availablePermits();
        }
        
        public int getMaxConnections() {
            return maxConnections;
        }
    }
}
