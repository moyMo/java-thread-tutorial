package com.threadtutorial.collections;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.Set;
import java.util.Random;

/**
 * ConcurrentHashMap并发哈希表示例
 * 演示线程安全的哈希表实现及其高级特性
 */
public class ConcurrentHashMapDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== ConcurrentHashMap并发哈希表示例 ===");
        
        System.out.println("\n1. 基本操作示例:");
        basicOperationsExample();
        
        System.out.println("\n2. 线程安全测试:");
        threadSafetyExample();
        
        System.out.println("\n3. 原子操作示例:");
        atomicOperationsExample();
        
        System.out.println("\n4. 搜索和转换操作:");
        searchAndTransformExample();
        
        System.out.println("\n5. 性能对比测试:");
        performanceComparisonExample();
        
        System.out.println("\n6. 高级特性示例:");
        advancedFeaturesExample();
    }
    
    /**
     * 基本操作示例
     */
    private static void basicOperationsExample() {
        ConcurrentMap<String, Integer> map = new ConcurrentHashMap<>();
        
        System.out.println("1) 添加元素:");
        map.put("Alice", 25);
        map.put("Bob", 30);
        map.put("Charlie", 35);
        map.put("David", 28);
        
        System.out.println("Map内容: " + map);
        System.out.println("大小: " + map.size());
        System.out.println("是否包含Alice: " + map.containsKey("Alice"));
        System.out.println("Alice的年龄: " + map.get("Alice"));
        
        System.out.println("\n2) 更新元素:");
        Integer oldValue = map.put("Alice", 26);
        System.out.println("Alice的旧年龄: " + oldValue + ", 新年龄: " + map.get("Alice"));
        
        System.out.println("\n3) 删除元素:");
        map.remove("Bob");
        System.out.println("删除Bob后: " + map);
        
        System.out.println("\n4) 遍历元素:");
        System.out.println("键值对遍历:");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
        }
        
        System.out.println("\n键遍历:");
        for (String key : map.keySet()) {
            System.out.println("  键: " + key);
        }
        
        System.out.println("\n值遍历:");
        for (Integer value : map.values()) {
            System.out.println("  值: " + value);
        }
    }
    
    /**
     * 线程安全测试
     */
    private static void threadSafetyExample() throws InterruptedException {
        System.out.println("测试ConcurrentHashMap的线程安全性:");
        
        ConcurrentMap<String, AtomicInteger> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // 初始化计数器
        map.put("counter", new AtomicInteger(0));
        
        // 创建100个任务，每个任务增加计数器100次
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    map.get("counter").incrementAndGet();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("最终计数器值: " + map.get("counter").get());
        System.out.println("期望值: 10000");
        System.out.println("测试结果: " + (map.get("counter").get() == 10000 ? "线程安全" : "线程不安全"));
    }
    
    /**
     * 原子操作示例
     */
    private static void atomicOperationsExample() {
        System.out.println("ConcurrentHashMap的原子操作:");
        
        ConcurrentMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("counter", 0);
        
        // 1. putIfAbsent - 如果键不存在则添加
        Integer result1 = map.putIfAbsent("counter", 100);
        System.out.println("putIfAbsent('counter', 100): " + result1 + " (map: " + map + ")");
        
        Integer result2 = map.putIfAbsent("newKey", 200);
        System.out.println("putIfAbsent('newKey', 200): " + result2 + " (map: " + map + ")");
        
        // 2. replace - 替换现有值
        boolean replaced1 = map.replace("counter", 0, 50);
        System.out.println("replace('counter', 0, 50): " + replaced1 + " (map: " + map + ")");
        
        boolean replaced2 = map.replace("counter", 100, 50);
        System.out.println("replace('counter', 100, 50): " + replaced2 + " (map: " + map + ")");
        
        // 3. remove - 条件删除
        boolean removed1 = map.remove("counter", 50);
        System.out.println("remove('counter', 50): " + removed1 + " (map: " + map + ")");
        
        boolean removed2 = map.remove("newKey", 300);
        System.out.println("remove('newKey', 300): " + removed2 + " (map: " + map + ")");
        
        // 4. compute - 原子计算
        map.put("visits", 0);
        map.compute("visits", (key, value) -> value + 1);
        System.out.println("compute增加访问次数: " + map);
        
        map.compute("visits", (key, value) -> value * 10);
        System.out.println("compute乘以10: " + map);
        
        // 5. merge - 合并值
        map.merge("visits", 5, (oldValue, newValue) -> oldValue + newValue);
        System.out.println("merge添加5: " + map);
    }
    
    /**
     * 搜索和转换操作
     */
    private static void searchAndTransformExample() {
        System.out.println("搜索和转换操作:");
        
        ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();
        scores.put("Alice", 85);
        scores.put("Bob", 92);
        scores.put("Charlie", 78);
        scores.put("David", 95);
        scores.put("Eve", 88);
        
        System.out.println("原始分数: " + scores);
        
        // 1. search - 搜索符合条件的条目
        String result1 = scores.search(2, (key, value) -> value > 90 ? key : null);
        System.out.println("搜索分数>90的学生: " + result1);
        
        // 2. forEach - 遍历所有条目
        System.out.println("\nforEach遍历:");
        scores.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        
        // 3. reduce - 归约操作
        int totalScore = scores.reduceValues(2, Integer::sum);
        System.out.println("\n总分: " + totalScore);
        
        double averageScore = scores.reduceValues(2, Integer::sum) / (double) scores.size();
        System.out.println("平均分: " + averageScore);
        
        // 4. transform - 转换值
        System.out.println("\n转换分数为等级:");
        scores.replaceAll((key, value) -> {
            if (value >= 90) return 1; // A
            else if (value >= 80) return 2; // B
            else if (value >= 70) return 3; // C
            else return 4; // D
        });
        System.out.println("转换后: " + scores);
    }
    
    /**
     * 性能对比测试
     */
    private static void performanceComparisonExample() throws InterruptedException {
        System.out.println("ConcurrentHashMap vs HashMap性能对比:");
        
        int numOperations = 100000;
        int numThreads = 10;
        
        // 测试ConcurrentHashMap
        long startTime = System.currentTimeMillis();
        testConcurrentHashMapPerformance(numOperations, numThreads);
        long concurrentTime = System.currentTimeMillis() - startTime;
        
        System.out.println("ConcurrentHashMap耗时: " + concurrentTime + "ms");
        
        // 注意：HashMap不是线程安全的，这里仅作对比参考
        System.out.println("\n注意：HashMap不是线程安全的，多线程环境下可能出现问题");
        System.out.println("ConcurrentHashMap专为高并发场景设计，性能更优");
    }
    
    private static void testConcurrentHashMapPerformance(int numOperations, int numThreads) 
            throws InterruptedException {
        ConcurrentMap<Integer, String> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < numOperations / numThreads; j++) {
                    int key = random.nextInt(1000);
                    map.put(key, "Value-" + threadId + "-" + j);
                    map.get(key);
                    if (random.nextDouble() > 0.7) {
                        map.remove(key);
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
    
    /**
     * 高级特性示例
     */
    private static void advancedFeaturesExample() {
        System.out.println("ConcurrentHashMap高级特性:");
        
        // 1. 创建时指定初始容量和负载因子
        ConcurrentMap<String, String> map1 = new ConcurrentHashMap<>(16, 0.75f, 8);
        System.out.println("1. 自定义初始容量(16)、负载因子(0.75)、并发级别(8)");
        
        // 2. 使用keySet和values的弱一致性迭代器
        ConcurrentMap<String, Integer> map2 = new ConcurrentHashMap<>();
        map2.put("A", 1);
        map2.put("B", 2);
        map2.put("C", 3);
        
        System.out.println("\n2. 弱一致性迭代器示例:");
        Set<String> keySet = map2.keySet();
        System.out.println("KeySet: " + keySet);
        
        // 在迭代期间修改map
        System.out.println("迭代期间添加新元素:");
        for (String key : keySet) {
            System.out.println("  迭代: " + key);
            if (key.equals("B")) {
                map2.put("D", 4); // 添加新元素
            }
        }
        System.out.println("迭代后Map内容: " + map2);
        
        // 3. 统计信息
        System.out.println("\n3. 统计信息:");
        System.out.println("大小: " + map2.size());
        System.out.println("是否为空: " + map2.isEmpty());
        System.out.println("包含键'B': " + map2.containsKey("B"));
        System.out.println("包含值3: " + map2.containsValue(3));
        
        // 4. 清空和克隆
        System.out.println("\n4. 清空操作:");
        ConcurrentMap<String, Integer> map3 = new ConcurrentHashMap<>(map2);
        System.out.println("克隆的Map: " + map3);
        map3.clear();
        System.out.println("清空后: " + map3);
        
        // 5. 批量操作
        System.out.println("\n5. 批量操作示例:");
        ConcurrentMap<String, String> bulkMap = new ConcurrentHashMap<>();
        
        // 批量添加
        bulkMap.putAll(Map.of(
            "key1", "value1",
            "key2", "value2",
            "key3", "value3"
        ));
        System.out.println("批量添加后: " + bulkMap);
    }
}
