package com.threadtutorial.projects;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 多线程任务调度器示例
 * 演示定时任务、周期性任务和延迟任务的调度
 */
public class TaskScheduler {

    // 任务调度器
    private final ScheduledExecutorService scheduler;
    
    // 任务统计
    private final AtomicInteger totalTasks = new AtomicInteger(0);
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);
    private final AtomicInteger runningTasks = new AtomicInteger(0);
    
    // 任务存储
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, TaskInfo> taskInfoMap = new ConcurrentHashMap<>();
    
    /**
     * 构造函数
     * @param threadCount 线程池大小
     */
    public TaskScheduler(int threadCount) {
        this.scheduler = Executors.newScheduledThreadPool(threadCount);
        System.out.println("任务调度器初始化完成，线程数: " + threadCount);
    }
    
    /**
     * 任务信息类
     */
    static class TaskInfo {
        String id;
        String name;
        TaskType type;
        long startTime;
        long nextExecutionTime;
        int executionCount;
        int maxExecutions;
        boolean isRunning;
        
        TaskInfo(String id, String name, TaskType type, int maxExecutions) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.startTime = System.currentTimeMillis();
            this.executionCount = 0;
            this.maxExecutions = maxExecutions;
            this.isRunning = false;
        }
    }
    
    /**
     * 任务类型枚举
     */
    enum TaskType {
        IMMEDIATE,      // 立即执行
        DELAYED,        // 延迟执行
        FIXED_RATE,     // 固定频率执行
        FIXED_DELAY     // 固定延迟执行
    }
    
    /**
     * 提交立即执行任务
     */
    public String submitImmediateTask(String taskName, Runnable task) {
        String taskId = generateTaskId(taskName);
        totalTasks.incrementAndGet();
        runningTasks.incrementAndGet();
        
        TaskInfo info = new TaskInfo(taskId, taskName, TaskType.IMMEDIATE, 1);
        taskInfoMap.put(taskId, info);
        
        scheduler.submit(() -> {
            info.isRunning = true;
            try {
                task.run();
                completedTasks.incrementAndGet();
                info.executionCount++;
            } catch (Exception e) {
                failedTasks.incrementAndGet();
                System.err.println("任务执行失败: " + taskName + ", 错误: " + e.getMessage());
            } finally {
                runningTasks.decrementAndGet();
                info.isRunning = false;
            }
        });
        
        System.out.println("提交立即任务: " + taskName + " (ID: " + taskId + ")");
        return taskId;
    }
    
    /**
     * 提交延迟执行任务
     */
    public String submitDelayedTask(String taskName, Runnable task, long delay, TimeUnit unit) {
        String taskId = generateTaskId(taskName);
        totalTasks.incrementAndGet();
        
        TaskInfo info = new TaskInfo(taskId, taskName, TaskType.DELAYED, 1);
        info.nextExecutionTime = System.currentTimeMillis() + unit.toMillis(delay);
        taskInfoMap.put(taskId, info);
        
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            runningTasks.incrementAndGet();
            info.isRunning = true;
            try {
                task.run();
                completedTasks.incrementAndGet();
                info.executionCount++;
            } catch (Exception e) {
                failedTasks.incrementAndGet();
                System.err.println("延迟任务执行失败: " + taskName + ", 错误: " + e.getMessage());
            } finally {
                runningTasks.decrementAndGet();
                info.isRunning = false;
                scheduledTasks.remove(taskId);
            }
        }, delay, unit);
        
        scheduledTasks.put(taskId, future);
        System.out.println("提交延迟任务: " + taskName + " (ID: " + taskId + 
                          ", 延迟: " + delay + " " + unit + ")");
        return taskId;
    }
    
    /**
     * 提交固定频率任务
     */
    public String submitFixedRateTask(String taskName, Runnable task, 
                                     long initialDelay, long period, TimeUnit unit,
                                     int maxExecutions) {
        String taskId = generateTaskId(taskName);
        totalTasks.incrementAndGet();
        
        TaskInfo info = new TaskInfo(taskId, taskName, TaskType.FIXED_RATE, maxExecutions);
        info.nextExecutionTime = System.currentTimeMillis() + unit.toMillis(initialDelay);
        taskInfoMap.put(taskId, info);
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (info.executionCount >= info.maxExecutions && info.maxExecutions > 0) {
                cancelTask(taskId);
                return;
            }
            
            runningTasks.incrementAndGet();
            info.isRunning = true;
            try {
                task.run();
                completedTasks.incrementAndGet();
                info.executionCount++;
                info.nextExecutionTime = System.currentTimeMillis() + unit.toMillis(period);
            } catch (Exception e) {
                failedTasks.incrementAndGet();
                System.err.println("固定频率任务执行失败: " + taskName + ", 错误: " + e.getMessage());
            } finally {
                runningTasks.decrementAndGet();
                info.isRunning = false;
            }
        }, initialDelay, period, unit);
        
        scheduledTasks.put(taskId, future);
        System.out.println("提交固定频率任务: " + taskName + " (ID: " + taskId + 
                          ", 初始延迟: " + initialDelay + " " + unit + 
                          ", 周期: " + period + " " + unit + 
                          ", 最大执行次数: " + maxExecutions + ")");
        return taskId;
    }
    
    /**
     * 提交固定延迟任务
     */
    public String submitFixedDelayTask(String taskName, Runnable task,
                                      long initialDelay, long delay, TimeUnit unit,
                                      int maxExecutions) {
        String taskId = generateTaskId(taskName);
        totalTasks.incrementAndGet();
        
        TaskInfo info = new TaskInfo(taskId, taskName, TaskType.FIXED_DELAY, maxExecutions);
        info.nextExecutionTime = System.currentTimeMillis() + unit.toMillis(initialDelay);
        taskInfoMap.put(taskId, info);
        
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(() -> {
            if (info.executionCount >= info.maxExecutions && info.maxExecutions > 0) {
                cancelTask(taskId);
                return;
            }
            
            runningTasks.incrementAndGet();
            info.isRunning = true;
            try {
                task.run();
                completedTasks.incrementAndGet();
                info.executionCount++;
                info.nextExecutionTime = System.currentTimeMillis() + unit.toMillis(delay);
            } catch (Exception e) {
                failedTasks.incrementAndGet();
                System.err.println("固定延迟任务执行失败: " + taskName + ", 错误: " + e.getMessage());
            } finally {
                runningTasks.decrementAndGet();
                info.isRunning = false;
            }
        }, initialDelay, delay, unit);
        
        scheduledTasks.put(taskId, future);
        System.out.println("提交固定延迟任务: " + taskName + " (ID: " + taskId + 
                          ", 初始延迟: " + initialDelay + " " + unit + 
                          ", 延迟: " + delay + " " + unit + 
                          ", 最大执行次数: " + maxExecutions + ")");
        return taskId;
    }
    
    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            if (cancelled) {
                scheduledTasks.remove(taskId);
                TaskInfo info = taskInfoMap.get(taskId);
                if (info != null) {
                    info.isRunning = false;
                }
                System.out.println("任务已取消: " + taskId);
            }
            return cancelled;
        }
        return false;
    }
    
    /**
     * 获取任务状态
     */
    public void printTaskStatus(String taskId) {
        TaskInfo info = taskInfoMap.get(taskId);
        if (info != null) {
            System.out.println("\n=== 任务状态 ===");
            System.out.println("任务ID: " + info.id);
            System.out.println("任务名称: " + info.name);
            System.out.println("任务类型: " + info.type);
            System.out.println("是否运行中: " + info.isRunning);
            System.out.println("执行次数: " + info.executionCount + "/" + info.maxExecutions);
            System.out.println("开始时间: " + new Date(info.startTime));
            if (info.nextExecutionTime > 0) {
                System.out.println("下次执行时间: " + new Date(info.nextExecutionTime));
            }
        } else {
            System.out.println("任务不存在: " + taskId);
        }
    }
    
    /**
     * 打印调度器统计信息
     */
    public void printStatistics() {
        System.out.println("\n=== 调度器统计信息 ===");
        System.out.println("总任务数: " + totalTasks.get());
        System.out.println("已完成任务: " + completedTasks.get());
        System.out.println("失败任务: " + failedTasks.get());
        System.out.println("运行中任务: " + runningTasks.get());
        System.out.println("计划中任务: " + scheduledTasks.size());
        System.out.println("成功率: " + 
            (totalTasks.get() > 0 ? 
             (completedTasks.get() * 100.0 / totalTasks.get()) : 0) + "%");
        
        System.out.println("\n=== 计划任务列表 ===");
        for (TaskInfo info : taskInfoMap.values()) {
            System.out.println("ID: " + info.id + 
                             ", 名称: " + info.name + 
                             ", 类型: " + info.type + 
                             ", 执行次数: " + info.executionCount);
        }
    }
    
    /**
     * 关闭调度器
     */
    public void shutdown() {
        System.out.println("正在关闭任务调度器...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("任务调度器已关闭");
    }
    
    /**
     * 生成任务ID
     */
    private String generateTaskId(String taskName) {
        return taskName + "-" + System.currentTimeMillis() + "-" + totalTasks.get();
    }
    
    /**
     * 示例任务
     */
    private static class ExampleTask implements Runnable {
        private final String name;
        private final int duration; // 模拟任务执行时间（毫秒）
        
        public ExampleTask(String name, int duration) {
            this.name = name;
            this.duration = duration;
        }
        
        @Override
        public void run() {
            System.out.println("[" + new Date() + "] 任务开始: " + name);
            try {
                // 模拟任务执行
                Thread.sleep(duration);
                System.out.println("[" + new Date() + "] 任务完成: " + name);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[" + new Date() + "] 任务中断: " + name);
            }
        }
    }
    
    /**
     * 主方法 - 演示使用
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 多线程任务调度器示例 ===");
        
        // 创建调度器
        TaskScheduler scheduler = new TaskScheduler(4);
        
        // 演示各种任务类型
        System.out.println("\n1. 提交立即执行任务:");
        String immediateTaskId = scheduler.submitImmediateTask(
            "数据清理任务", new ExampleTask("数据清理", 500));
        
        System.out.println("\n2. 提交延迟执行任务:");
        String delayedTaskId = scheduler.submitDelayedTask(
            "报表生成任务", new ExampleTask("报表生成", 800), 2, TimeUnit.SECONDS);
        
        System.out.println("\n3. 提交固定频率任务:");
        String fixedRateTaskId = scheduler.submitFixedRateTask(
            "心跳检测任务", new ExampleTask("心跳检测", 300), 
            1, 3, TimeUnit.SECONDS, 3);
        
        System.out.println("\n4. 提交固定延迟任务:");
        String fixedDelayTaskId = scheduler.submitFixedDelayTask(
            "数据同步任务", new ExampleTask("数据同步", 600), 
            2, 5, TimeUnit.SECONDS, 2);
        
        // 等待一段时间查看任务执行情况
        Thread.sleep(5000);
        
        // 查看任务状态
        System.out.println("\n5. 查看任务状态:");
        scheduler.printTaskStatus(fixedRateTaskId);
        
        // 取消一个任务
        System.out.println("\n6. 取消任务:");
        scheduler.cancelTask(delayedTaskId);
        
        // 等待更多时间
        Thread.sleep(8000);
        
        // 打印统计信息
        scheduler.printStatistics();
        
        // 关闭调度器
        scheduler.shutdown();
        
        System.out.println("\n=== 调度器技术要点 ===");
        System.out.println("1. ScheduledExecutorService: Java内置的定时任务调度器");
        System.out.println("2. 任务类型: 立即、延迟、固定频率、固定延迟");
        System.out.println("3. 线程池管理: 控制并发任务数量");
        System.out.println("4. 任务监控: 跟踪任务状态和执行统计");
        System.out.println("5. 错误处理: 捕获任务执行异常");
        System.out.println("6. 资源管理: 合理关闭调度器");
        
        System.out.println("\n=== 实际应用场景 ===");
        System.out.println("- 定时数据备份和清理");
        System.out.println("- 系统监控和报警");
        System.out.println("- 缓存刷新和数据同步");
        System.out.println("- 报表生成和邮件发送");
        System.out.println("- 心跳检测和连接保持");
    }
    
    /**
     * 高级特性演示
     */
    public static void advancedDemo() {
        System.out.println("\n=== 高级特性演示 ===");
        
        System.out.println("1. 动态调整任务:");
        System.out.println("   - 根据系统负载动态调整任务执行频率");
        System.out.println("   - 根据任务优先级调整执行顺序");
        System.out.println("   - 实现任务依赖关系调度");
        
        System.out.println("\n2. 分布式任务调度:");
        System.out.println("   - 使用分布式锁保证任务唯一性");
        System.out.println("   - 实现任务分片和并行处理");
        System.out.println("   - 支持故障转移和负载均衡");
        
        System.out.println("\n3. 任务持久化:");
        System.out.println("   - 将任务信息保存到数据库");
        System.out.println("   - 支持系统重启后任务恢复");
        System.out.println("   - 实现任务历史记录和审计");
        
        System.out.println("\n4. 监控和告警:");
        System.out.println("   - 实时监控任务执行状态");
        System.out.println("   - 设置任务执行超时告警");
        System.out.println("   - 实现任务执行失败重试机制");
        
        System.out.println("\n5. 性能优化:");
        System.out.println("   - 使用合适的线程池大小");
        System.out.println("   - 避免任务执行时间过长");
        System.out.println("   - 合理设置任务执行频率");
        System.out.println("   - 使用异步非阻塞任务");
    }
}
