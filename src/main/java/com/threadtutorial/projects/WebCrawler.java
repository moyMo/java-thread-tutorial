package com.threadtutorial.projects;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程网络爬虫示例
 * 演示使用线程池和并发集合实现网页爬虫
 */
@Slf4j
public class WebCrawler {

    // 已访问的URL集合（线程安全）
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    
    // 待爬取的URL队列
    private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    
    // 线程池
    private final ExecutorService executorService;
    
    // 统计信息
    private final AtomicInteger totalPages = new AtomicInteger(0);
    private final AtomicInteger successfulFetches = new AtomicInteger(0);
    private final AtomicInteger failedFetches = new AtomicInteger(0);
    
    // 最大爬取页面数
    private final int maxPages;
    
    // 爬虫状态
    private volatile boolean crawling = false;
    
    /**
     * 构造函数
     * @param maxPages 最大爬取页面数
     * @param threadCount 线程数
     */
    public WebCrawler(int maxPages, int threadCount) {
        this.maxPages = maxPages;
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }
    
    /**
     * 启动爬虫
     * @param startUrl 起始URL
     */
    public void start(String startUrl) {
        if (crawling) {
            log.info("爬虫已经在运行中");
            return;
        }
        
        crawling = true;
        log.info("=== 启动网络爬虫 ===");
        log.info("起始URL:  {}", startUrl);
        log.info("最大页面数:  {}", maxPages);
        log.info("线程数:  {}", ((ThreadPoolExecutor) executorService).getMaximumPoolSize());
        
        // 添加起始URL到队列
        urlQueue.add(startUrl);
        
        // 启动爬虫线程
        for (int i = 0; i < ((ThreadPoolExecutor) executorService).getMaximumPoolSize(); i++) {
            executorService.submit(new CrawlerWorker("Worker-" + (i + 1)));
        }
        
        // 监控线程
        executorService.submit(new Monitor());
        
        // 等待爬虫完成
        try {
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        crawling = false;
        printStatistics();
    }
    
    /**
     * 爬虫工作线程
     */
    private class CrawlerWorker implements Runnable {
        private final String workerName;
        
        public CrawlerWorker(String workerName) {
            this.workerName = workerName;
        }
        
        @Override
        public void run() {
            log.info("{}  开始工作", workerName );
            
            while (crawling && totalPages.get() < maxPages) {
                try {
                    // 从队列获取URL（最多等待1秒）
                    String url = urlQueue.poll(1, TimeUnit.SECONDS);
                    if (url == null) {
                        // 队列为空，检查是否还有工作
                        if (visitedUrls.size() >= maxPages) {
                            break;
                        }
                        continue;
                    }
                    
                    // 检查是否已访问
                    if (visitedUrls.contains(url)) {
                        continue;
                    }
                    
                    // 标记为已访问
                    visitedUrls.add(url);
                    totalPages.incrementAndGet();
                    
                    // 爬取页面
                    log.info("{} 爬取: {}", workerName, url);
                    String content = fetchPage(url);
                    
                    if (content != null) {
                        successfulFetches.incrementAndGet();
                        
                        // 解析页面中的链接
                        List<String> links = extractLinks(content, url);
                        for (String link : links) {
                            if (!visitedUrls.contains(link) && totalPages.get() < maxPages) {
                                urlQueue.offer(link);
                            }
                        }
                        
                        // 模拟处理延迟
                        Thread.sleep(100);
                    } else {
                        failedFetches.incrementAndGet();
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println(workerName + " 错误: " + e.getMessage());
                    failedFetches.incrementAndGet();
                }
            }
            
            log.info("{}  结束工作", workerName );
        }
        
        /**
         * 获取页面内容（模拟）
         */
        private String fetchPage(String url) {
            try {
                // 模拟网络延迟
                Thread.sleep(new Random().nextInt(200));
                
                // 模拟页面内容
                return "<html><body><a href='http://example.com/page1'>Page 1</a>" +
                       "<a href='http://example.com/page2'>Page 2</a>" +
                       "<a href='http://example.com/page3'>Page 3</a></body></html>";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        
        /**
         * 从页面内容提取链接（模拟）
         */
        private List<String> extractLinks(String content, String baseUrl) {
            List<String> links = new ArrayList<>();
            Random random = new Random();
            
            // 模拟提取3-5个链接
            int linkCount = 3 + random.nextInt(3);
            for (int i = 1; i <= linkCount; i++) {
                links.add("http://example.com/page" + (visitedUrls.size() + i));
            }
            
            return links;
        }
    }
    
    /**
     * 监控线程
     */
    private class Monitor implements Runnable {
        @Override
        public void run() {
            log.info("监控线程启动");
            
            while (crawling && totalPages.get() < maxPages) {
                try {
                    Thread.sleep(2000); // 每2秒报告一次
                    
                    log.info("\n=== 爬虫状态报告 ===");
                    log.info("已访问页面:  {}", visitedUrls.size() + "/" + maxPages);
                    log.info("队列大小:  {}", urlQueue.size());
                    log.info("成功爬取:  {}", successfulFetches.get());
                    log.info("失败爬取:  {}", failedFetches.get());
                    log.info("活跃线程:  {}", ((ThreadPoolExecutor) executorService).getActiveCount());
                    log.info("===================\n");
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            log.info("监控线程结束");
        }
    }
    
    /**
     * 打印统计信息
     */
    private void printStatistics() {
        log.info("\n=== 爬虫统计信息 ===");
        log.info("总页面数:  {}", totalPages.get());
        log.info("成功爬取:  {}", successfulFetches.get());
        log.info("失败爬取:  {}", failedFetches.get());
        log.info("成功率:  {}", (totalPages.get() > 0 ? 
             (successfulFetches.get() * 100.0 / totalPages.get()) : 0) + "%");
        log.info("已访问URL示例:");
        
        int count = 0;
        for (String url : visitedUrls) {
            if (count++ >= 5) break;
            log.info("  -  {}", url);
        }
        if (visitedUrls.size() > 5) {
            log.info("  ... 还有  {}", (visitedUrls.size() - 5) + " 个URL");
        }
    }
    
    /**
     * 停止爬虫
     */
    public void stop() {
        crawling = false;
        executorService.shutdownNow();
        log.info("爬虫已停止");
    }
    
    /**
     * 主方法 - 演示使用
     */
    public static void main(String[] args) {
        log.info("=== 多线程网络爬虫示例 ===");
        
        // 创建爬虫实例
        WebCrawler crawler = new WebCrawler(20, 4);
        
        // 启动爬虫
        crawler.start("http://example.com");
        
        log.info("\n=== 爬虫技术要点 ===");
        log.info("1. 线程池管理: 使用固定大小线程池控制并发度");
        log.info("2. 并发集合: 使用ConcurrentHashMap和BlockingQueue保证线程安全");
        log.info("3. 流量控制: 限制最大爬取页面数，防止无限爬取");
        log.info("4. 状态监控: 独立监控线程报告爬虫状态");
        log.info("5. 错误处理: 统计成功/失败次数，提高健壮性");
        log.info("6. 资源管理: 合理关闭线程池，防止资源泄漏");
        
        log.info("\n=== 扩展建议 ===");
        log.info("1. 添加URL去重策略（布隆过滤器）");
        log.info("2. 实现真实的HTTP请求和HTML解析");
        log.info("3. 添加 robots.txt 遵守规则");
        log.info("4. 实现深度优先或广度优先搜索策略");
        log.info("5. 添加持久化存储（数据库或文件）");
        log.info("6. 实现分布式爬虫架构");
    }
    
    /**
     * 高级特性演示
     */
    public static void advancedDemo() {
        log.info("\n=== 高级特性演示 ===");
        
        // 演示不同的线程池配置
        log.info("1. 不同线程池配置对比:");
        
        // 小线程池
        WebCrawler smallPoolCrawler = new WebCrawler(10, 2);
        log.info("小线程池(2线程) - 适合低并发场景");
        
        // 大线程池
        WebCrawler largePoolCrawler = new WebCrawler(10, 8);
        log.info("大线程池(8线程) - 适合高并发场景");
        
        // 演示不同的队列策略
        log.info("\n2. 不同队列策略:");
        log.info("LinkedBlockingQueue - 无界队列，适合生产快消费慢的场景");
        log.info("ArrayBlockingQueue - 有界队列，防止内存溢出");
        log.info("PriorityBlockingQueue - 优先级队列，按重要性爬取");
        
        // 演示监控和调优
        log.info("\n3. 性能监控和调优:");
        log.info("- 监控队列大小，调整生产者/消费者比例");
        log.info("- 监控线程活跃度，调整线程池大小");
        log.info("- 统计成功率，优化错误处理策略");
        log.info("- 分析爬取延迟，优化网络请求");
        
        log.info("\n4. 实际应用场景:");
        log.info("- 搜索引擎数据收集");
        log.info("- 价格监控和比价");
        log.info("- 新闻聚合");
        log.info("- 社交媒体分析");
        log.info("- 学术研究数据收集");
    }
}
