package com.threadtutorial.tools;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * CompletableFuture异步编程示例
 * 演示Java 8+的CompletableFuture异步编程特性
 */
@Slf4j
public class CompletableFutureDemo {

    public static void main(String[] args) throws Exception {
        log.info("=== CompletableFuture异步编程示例 ===");
        
        log.info("\n1. 基本用法 - 创建和获取结果:");
        basicUsageExample();
        
        log.info("\n2. 链式调用 - thenApply, thenAccept, thenRun:");
        chainExample();
        
        log.info("\n3. 组合多个Future - thenCompose, thenCombine:");
        combineExample();
        
        log.info("\n4. 并行执行 - allOf, anyOf:");
        parallelExample();
        
        log.info("\n5. 异常处理 - exceptionally, handle:");
        exceptionHandlingExample();
        
        log.info("\n6. 超时控制 - orTimeout, completeOnTimeout:");
        timeoutExample();
        
        log.info("\n7. 自定义线程池:");
        customExecutorExample();
    }
    
    /**
     * 基本用法示例
     */
    private static void basicUsageExample() throws ExecutionException, InterruptedException {
        // 1. 使用runAsync执行无返回值的任务
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            log.info("runAsync: 执行无返回值任务 -  {}", Thread.currentThread().getName());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
        });
        future1.get(); // 等待完成
        
        // 2. 使用supplyAsync执行有返回值的任务
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            log.info("supplyAsync: 执行有返回值任务 -  {}", Thread.currentThread().getName());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "Hello from CompletableFuture!";
        });
        
        String result = future2.get();
        log.info("获取结果:  {}", result);
        
        // 3. 异步回调 - 不阻塞获取结果
        CompletableFuture.supplyAsync(() -> {
            log.info("异步任务执行中...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return 42;
        }).thenAccept(value -> {
            log.info("异步回调收到结果:  {}", value);
        });
        
        // 等待异步任务完成
        Thread.sleep(1500);
    }
    
    /**
     * 链式调用示例
     */
    private static void chainExample() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            log.info("第一步: 获取用户ID");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "user123";
        })
        .thenApply(userId -> {
            log.info("第二步: 根据用户ID查询用户信息");
            return "用户: " + userId + ", 姓名: 张三";
        })
        .thenApply(userInfo -> {
            log.info("第三步: 获取用户订单");
            return userInfo + ", 订单数: 5";
        })
        .thenApply(orderInfo -> {
            log.info("第四步: 计算订单总金额");
            return orderInfo + ", 总金额: ¥1280.00";
        });
        
        String finalResult = future.get();
        log.info("最终结果:  {}", finalResult);
        
        // thenAccept和thenRun示例
        CompletableFuture.supplyAsync(() -> "测试数据")
            .thenAccept(result -> log.info("thenAccept消费结果:  {}", result))
            .thenRun(() -> System.out.println("thenRun执行后续操作"))
            .get();
    }
    
    /**
     * 组合多个Future示例
     */
    private static void combineExample() throws ExecutionException, InterruptedException {
        // 模拟获取用户信息的任务
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            log.info("获取用户信息...");
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "用户: 张三";
        });
        
        // 模拟获取订单信息的任务
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> {
            log.info("获取订单信息...");
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "订单: #1001";
        });
        
        // thenCombine - 合并两个独立Future的结果
        CompletableFuture<String> combinedFuture = userFuture.thenCombine(orderFuture, 
            (user, order) -> user + " - " + order + " - 状态: 已支付");
        
        log.info("合并结果:  {}", combinedFuture.get());
        
        // thenCompose - 链式依赖（一个Future的结果作为另一个Future的输入）
        CompletableFuture<String> chainedFuture = userFuture.thenCompose(user -> 
            CompletableFuture.supplyAsync(() -> {
                log.info("根据用户信息获取详细信息...");
                return user + " - 年龄: 30 - 城市: 北京";
            })
        );
        
        log.info("链式结果:  {}", chainedFuture.get());
    }
    
    /**
     * 并行执行示例
     */
    private static void parallelExample() throws ExecutionException, InterruptedException {
        // 创建多个并行任务
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            log.info("任务1开始执行");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "任务1完成";
        });
        
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            log.info("任务2开始执行");
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "任务2完成";
        });
        
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            log.info("任务3开始执行");
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "任务3完成";
        });
        
        // allOf - 等待所有任务完成
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
        allTasks.thenRun(() -> {
            log.info("\n所有任务都已完成!");
            try {
                log.info("任务1结果:  {}", task1.get());
                log.info("任务2结果:  {}", task2.get());
                log.info("任务3结果:  {}", task3.get());
            } catch (Exception e) {
                log.error("异常", e);
            }
        }).get();
        
        // anyOf - 任意一个任务完成
        CompletableFuture<Object> anyTask = CompletableFuture.anyOf(task1, task2, task3);
        Object firstResult = anyTask.get();
        log.info("第一个完成的任务结果:  {}", firstResult);
    }
    
    /**
     * 异常处理示例
     */
    private static void exceptionHandlingExample() throws ExecutionException, InterruptedException {
        // exceptionally - 异常时提供默认值
        CompletableFuture<String> futureWithException = CompletableFuture.supplyAsync(() -> {
            log.info("执行可能失败的任务...");
            if (Math.random() > 0.5) {
                throw new RuntimeException("任务执行失败!");
            }
            return "任务成功";
        }).exceptionally(ex -> {
            log.info("捕获异常:  {}", ex.getMessage());
            return "默认值（异常时返回）";
        });
        
        log.info("结果（带异常处理）:  {}", futureWithException.get());
        
        // handle - 无论成功失败都处理
        CompletableFuture<String> futureWithHandle = CompletableFuture.supplyAsync(() -> {
            log.info("另一个可能失败的任务...");
            if (Math.random() > 0.5) {
                throw new RuntimeException("又失败了!");
            }
            return "成功结果";
        }).handle((result, ex) -> {
            if (ex != null) {
                return "处理异常: " + ex.getMessage();
            }
            return "处理成功结果: " + result;
        });
        
        log.info("handle结果:  {}", futureWithHandle.get());
    }
    
    /**
     * 超时控制示例
     */
    private static void timeoutExample() throws Exception {
        // orTimeout - 超时抛出TimeoutException
        CompletableFuture<String> timeoutFuture = CompletableFuture.supplyAsync(() -> {
            log.info("执行长时间任务...");
            try {
                Thread.sleep(2000); // 任务需要2秒
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "任务完成";
        }).orTimeout(1, TimeUnit.SECONDS); // 设置1秒超时
        
        try {
            String result = timeoutFuture.get();
            log.info("结果:  {}", result);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TimeoutException) {
                log.info("任务超时:  {}", e.getCause().getMessage());
            }
        }
        
        // completeOnTimeout - 超时提供默认值
        CompletableFuture<String> timeoutWithDefault = CompletableFuture.supplyAsync(() -> {
            log.info("另一个长时间任务...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
            return "任务完成";
        }).completeOnTimeout("超时默认值", 1, TimeUnit.SECONDS);
        
        log.info("超时控制结果:  {}", timeoutWithDefault.get());
    }
    
    /**
     * 自定义线程池示例
     */
    private static void customExecutorExample() throws ExecutionException, InterruptedException {
        ExecutorService customExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread thread = new Thread(r);
            thread.setName("CustomPool-" + thread.getId());
            thread.setDaemon(false);
            return thread;
        });
        
        log.info("使用自定义线程池执行CompletableFuture任务:");
        
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            log.info("任务1 - 线程:  {}", Thread.currentThread().getName());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
        }, customExecutor);
        
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            log.info("任务2 - 线程:  {}", Thread.currentThread().getName());
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
        }, customExecutor);
        
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            log.info("任务3 - 线程:  {}", Thread.currentThread().getName());
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                log.error("异常", e);
            }
        }, customExecutor);
        
        CompletableFuture.allOf(future1, future2, future3).get();
        
        // 关闭自定义线程池
        customExecutor.shutdown();
        customExecutor.awaitTermination(2, TimeUnit.SECONDS);
        log.info("自定义线程池示例完成");
    }
}
