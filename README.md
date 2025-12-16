# Java线程工具使用教程

这是一个全面的Java多线程编程教程项目，专注于Java线程工具的使用和实践。项目涵盖了从基础线程概念到高级并发工具的各种示例，适合初学者和有一定经验的开发者。

## 项目特点

- **系统性学习路径**：从基础到高级，循序渐进
- **实用示例**：每个概念都有可运行的代码示例
- **工具全覆盖**：涵盖Java并发包中的所有重要工具
- **最佳实践**：包含多线程编程的最佳实践和常见陷阱
- **综合项目**：提供实际应用场景的综合示例

## 项目结构

```
java-thread-tutorial/
├── pom.xml                    # Maven项目配置文件
├── README.md                  # 项目说明文档
├── basics/                    # 线程基础
│   ├── ThreadCreation.java   # 线程创建方式
│   ├── ThreadLifecycle.java  # 线程生命周期
│   ├── ThreadSynchronization.java # 线程同步
│   └── ThreadCommunication.java  # 线程通信
├── tools/                     # 线程工具
│   ├── ExecutorServiceDemo.java # 线程池执行器
│   ├── ThreadPoolDemo.java   # 线程池使用
│   ├── CompletableFutureDemo.java # 异步编程
│   └── ForkJoinPoolDemo.java # 分治线程池
├── concurrency/               # 并发工具
│   ├── CountDownLatchDemo.java # 倒计时门闩
│   └── SemaphoreDemo.java    # 信号量
├── collections/               # 线程安全集合
│   ├── ConcurrentHashMapDemo.java # 并发哈希表
│   ├── CopyOnWriteArrayListDemo.java # 写时复制列表
│   ├── BlockingQueueDemo.java # 阻塞队列
│   └── ConcurrentLinkedQueueDemo.java # 并发链表队列
└── projects/                  # 综合项目
    ├── WebCrawler.java       # 多线程网页爬虫
    ├── TaskScheduler.java    # 任务调度器
    ├── ProducerConsumer.java # 生产者消费者模式
    └── BankTransferSimulation.java # 银行转账模拟
```

## 学习路径

### 第一阶段：线程基础
1. 线程的创建方式（继承Thread类 vs 实现Runnable接口）
2. 线程的生命周期管理
3. 线程同步机制（synchronized, Lock）
4. 线程间通信（wait/notify）

### 第二阶段：线程工具
1. ExecutorService框架
2. 线程池配置和使用
3. CompletableFuture异步编程
4. ForkJoinPool并行计算

### 第三阶段：并发工具
1. CountDownLatch倒计时门闩
2. Semaphore信号量控制

### 第四阶段：线程安全集合
1. ConcurrentHashMap并发映射
2. CopyOnWriteArrayList写时复制
3. BlockingQueue阻塞队列
4. 其他并发集合类

### 第五阶段：综合应用
1. 实际项目中的多线程应用
2. 性能优化和调试技巧
3. 常见问题和解决方案

## 环境要求

- Java 11或更高版本
- Maven 3.6或更高版本
- IDE推荐：IntelliJ IDEA, Eclipse, VS Code

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/yourusername/java-thread-tutorial.git
cd java-thread-tutorial
```

### 2. 编译项目
```bash
mvn clean compile
```

### 3. 运行示例
```bash
# 运行基础示例
mvn exec:java -Dexec.mainClass="com.threadtutorial.basics.ThreadCreation"

# 运行工具示例
mvn exec:java -Dexec.mainClass="com.threadtutorial.tools.ExecutorServiceDemo"
```

### 4. 运行测试
```bash
mvn test
```

## 使用说明

1. **按顺序学习**：建议按照目录结构从basics开始学习
2. **动手实践**：每个示例都可以直接运行，建议修改参数观察不同效果
3. **调试技巧**：使用Thread.currentThread().getName()跟踪线程执行
4. **性能监控**：使用JConsole或VisualVM监控线程状态

## 最佳实践

### 线程创建
- 优先使用线程池而不是直接创建线程
- 为线程设置有意义的名称便于调试
- 合理设置线程优先级

### 资源管理
- 确保正确关闭线程池
- 使用try-with-resources管理资源
- 避免线程泄漏

### 同步控制
- 尽量减小同步代码块的范围
- 使用并发集合替代手动同步
- 注意死锁和活锁问题

## 常见问题

### Q: 如何选择合适的线程池大小？
A: 根据任务类型（CPU密集型 vs IO密集型）和系统资源决定。CPU密集型任务建议使用CPU核心数+1，IO密集型任务可以设置更大的线程池。

### Q: synchronized和Lock有什么区别？
A: synchronized是Java关键字，使用简单但功能有限；Lock接口提供了更灵活的锁机制，支持尝试获取锁、可中断锁等高级功能。

### Q: 如何处理线程异常？
A: 使用UncaughtExceptionHandler捕获未处理的异常，避免线程 silently失败。

## 贡献指南

欢迎提交Issue和Pull Request来改进本教程！

1. Fork本仓库
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请通过GitHub Issues提交。
