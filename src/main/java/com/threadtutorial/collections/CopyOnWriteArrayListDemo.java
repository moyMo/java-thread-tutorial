package com.threadtutorial.collections;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CopyOnWriteArrayList写时复制列表示例
 * 演示写时复制集合的线程安全特性和适用场景
 */
@Slf4j
public class CopyOnWriteArrayListDemo {

    public static void main(String[] args) throws Exception {
        log.info("=== CopyOnWriteArrayList写时复制列表示例 ===");
        
        log.info("\n1. 基本操作示例:");
        basicOperationsExample();
        
        log.info("\n2. 线程安全测试 - 读写并发:");
        readWriteConcurrencyExample();
        
        log.info("\n3. 迭代器特性 - 快照迭代器:");
        iteratorSnapshotExample();
        
        log.info("\n4. 性能特点分析:");
        performanceCharacteristicsExample();
        
        log.info("\n5. 适用场景演示:");
        useCaseExample();
        
        log.info("\n6. CopyOnWriteArraySet示例:");
        copyOnWriteArraySetExample();
        
        log.info("\n7. 与普通ArrayList对比:");
        comparisonWithArrayList();
    }
    
    /**
     * 基本操作示例
     */
    private static void basicOperationsExample() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        
        log.info("1) 添加元素:");
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        list.add(1, "Blueberry"); // 在指定位置插入
        log.info("添加后:  {}", list);
        
        log.info("\n2) 获取和修改元素:");
        log.info("索引1的元素:  {}", list.get(1));
        list.set(1, "Blackberry");
        log.info("修改索引1后:  {}", list);
        
        log.info("\n3) 删除元素:");
        list.remove("Cherry");
        list.remove(0);
        log.info("删除后:  {}", list);
        
        log.info("\n4) 其他操作:");
        log.info("大小:  {}", list.size());
        log.info("是否为空:  {}", list.isEmpty());
        log.info("包含'Banana':  {}", list.contains("Banana"));
        log.info("索引'Blackberry':  {}", list.indexOf("Blackberry"));
        
        log.info("\n5) 批量操作:");
        list.addAll(List.of("Durian", "Elderberry", "Fig"));
        log.info("批量添加后:  {}", list);
        
        list.removeAll(List.of("Durian", "Fig"));
        log.info("批量删除后:  {}", list);
    }
    
    /**
     * 读写并发测试
     */
    private static void readWriteConcurrencyExample() throws InterruptedException {
        log.info("测试CopyOnWriteArrayList的读写并发安全性:");
        
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger readCount = new AtomicInteger(0);
        AtomicInteger writeCount = new AtomicInteger(0);
        
        // 初始化列表
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        
        // 创建读线程（5个）
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < 100; j++) {
                    // 随机读取
                    if (!list.isEmpty()) {
                        int index = random.nextInt(list.size());
                        Integer value = list.get(index);
                        readCount.incrementAndGet();
                    }
                    try {
                        Thread.sleep(random.nextInt(10));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        // 创建写线程（5个）
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < 20; j++) {
                    // 随机添加或删除
                    if (random.nextBoolean()) {
                        list.add(threadId * 100 + j);
                        writeCount.incrementAndGet();
                    } else if (!list.isEmpty()) {
                        int index = random.nextInt(list.size());
                        list.remove(index);
                        writeCount.incrementAndGet();
                    }
                    try {
                        Thread.sleep(random.nextInt(50));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        log.info("读操作次数:  {}", readCount.get());
        log.info("写操作次数:  {}", writeCount.get());
        log.info("最终列表大小:  {}", list.size());
        log.info("测试完成，无并发异常");
    }
    
    /**
     * 迭代器快照特性
     */
    private static void iteratorSnapshotExample() {
        log.info("演示CopyOnWriteArrayList迭代器的快照特性:");
        
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        
        log.info("原始列表:  {}", list);
        
        // 获取迭代器（创建快照）
        Iterator<String> iterator = list.iterator();
        
        log.info("\n获取迭代器后修改列表:");
        list.add("D");
        list.remove("B");
        log.info("修改后列表:  {}", list);
        
        log.info("\n迭代器遍历（显示快照内容）:");
        System.out.print("迭代器内容: ");
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        
        log.info("\n\n获取新迭代器:");
        Iterator<String> newIterator = list.iterator();
        System.out.print("新迭代器内容: ");
        while (newIterator.hasNext()) {
            System.out.print(newIterator.next() + " ");
        }
        
        log.info("\n\n重要特性：迭代器创建时创建列表的快照副本");
        log.info("迭代期间对列表的修改不会影响迭代器");
        log.info("这保证了迭代的一致性，但可能看到过时数据");
    }
    
    /**
     * 性能特点分析
     */
    private static void performanceCharacteristicsExample() {
        log.info("CopyOnWriteArrayList性能特点分析:");
        
        log.info("\n1. 读操作:");
        log.info("  - 非常快速，直接访问内部数组");
        log.info("  - 不需要同步，支持高并发读");
        log.info("  - 时间复杂度: O(1)");
        
        log.info("\n2. 写操作:");
        log.info("  - 较慢，需要复制整个数组");
        log.info("  - 写操作是同步的，但不会阻塞读操作");
        log.info("  - 时间复杂度: O(n)");
        
        log.info("\n3. 迭代操作:");
        log.info("  - 使用快照，迭代期间安全");
        log.info("  - 不会抛出ConcurrentModificationException");
        log.info("  - 可能迭代到过时数据");
        
        log.info("\n4. 内存使用:");
        log.info("  - 每次修改都创建新数组，内存开销大");
        log.info("  - 适合读多写少的场景");
        
        log.info("\n5. 适用场景:");
        log.info("  ✓ 读操作远多于写操作");
        log.info("  ✓ 需要遍历的安全性和一致性");
        log.info("  ✓ 集合大小较小或中等");
        log.info("  ✗ 频繁修改的大型集合");
        log.info("  ✗ 实时性要求高的场景");
    }
    
    /**
     * 适用场景演示
     */
    private static void useCaseExample() throws InterruptedException {
        log.info("CopyOnWriteArrayList适用场景演示:");
        
        // 场景：事件监听器列表（读多写少）
        CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
        
        // 添加监听器（写操作较少）
        listeners.add(() -> log.info("监听器1: 事件处理"));
        listeners.add(() -> log.info("监听器2: 事件处理"));
        listeners.add(() -> log.info("监听器3: 事件处理"));
        
        log.info("初始化监听器数量:  {}", listeners.size());
        
        // 模拟事件触发（大量读/遍历操作）
        ExecutorService executor = Executors.newFixedThreadPool(5);
        AtomicInteger eventCount = new AtomicInteger(0);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                // 触发事件 - 遍历所有监听器（读操作）
                for (Runnable listener : listeners) {
                    listener.run();
                    eventCount.incrementAndGet();
                }
            });
        }
        
        // 在事件处理期间添加新监听器（写操作）
        Thread.sleep(100);
        listeners.add(() -> log.info("新监听器: 动态添加"));
        
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        
        log.info("\n事件处理总数:  {}", eventCount.get());
        log.info("最终监听器数量:  {}", listeners.size());
        log.info("\n场景总结：");
        log.info("- 监听器列表读多写少，适合CopyOnWriteArrayList");
        log.info("- 事件触发时安全遍历，即使有新的监听器添加");
        log.info("- 新监听器对后续事件生效，不影响正在处理的事件");
    }
    
    /**
     * CopyOnWriteArraySet示例
     */
    private static void copyOnWriteArraySetExample() {
        log.info("CopyOnWriteArraySet示例（基于CopyOnWriteArrayList）:");
        
        CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
        
        log.info("1) 添加元素:");
        set.add("Apple");
        set.add("Banana");
        set.add("Apple"); // 重复元素
        set.add("Cherry");
        log.info("添加后:  {}", set);
        
        log.info("\n2) 集合操作:");
        log.info("大小:  {}", set.size());
        log.info("是否包含'Banana':  {}", set.contains("Banana"));
        log.info("是否为空:  {}", set.isEmpty());
        
        log.info("\n3) 遍历操作:");
        System.out.print("元素: ");
        for (String fruit : set) {
            System.out.print(fruit + " ");
        }
        
        log.info("\n\n4) 线程安全测试:");
        CopyOnWriteArraySet<Integer> threadSafeSet = new CopyOnWriteArraySet<>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        for (int i = 0; i < 10; i++) {
            final int value = i;
            executor.submit(() -> {
                threadSafeSet.add(value);
                threadSafeSet.contains(value);
                threadSafeSet.remove(value % 3);
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("异常", e);
        }
        
        log.info("并发操作后集合:  {}", threadSafeSet);
        log.info("无并发异常，线程安全");
    }
    
    /**
     * 与普通ArrayList对比
     */
    private static void comparisonWithArrayList() {
        log.info("CopyOnWriteArrayList vs ArrayList对比:");
        
        log.info("\n1. 线程安全性:");
        log.info("  CopyOnWriteArrayList: 线程安全，适合多线程环境");
        log.info("  ArrayList: 非线程安全，需要外部同步");
        
        log.info("\n2. 并发修改异常:");
        log.info("  CopyOnWriteArrayList: 迭代时修改不会抛异常");
        log.info("  ArrayList: 迭代时修改会抛ConcurrentModificationException");
        
        log.info("\n3. 性能特点:");
        log.info("  CopyOnWriteArrayList: 读快写慢，内存开销大");
        log.info("  ArrayList: 读写均衡，内存效率高");
        
        log.info("\n4. 迭代器行为:");
        log.info("  CopyOnWriteArrayList: 快照迭代器，看到创建时的状态");
        log.info("  ArrayList: 快速失败迭代器，检测到修改就失败");
        
        log.info("\n5. 使用建议:");
        log.info("  使用CopyOnWriteArrayList当:");
        log.info("    - 读操作远多于写操作");
        log.info("    - 需要遍历时线程安全");
        log.info("    - 集合大小较小");
        log.info("    - 可以接受数据暂时不一致");
        
        log.info("\n  使用ArrayList当:");
        log.info("    - 单线程环境");
        log.info("    - 读写操作均衡");
        log.info("    - 需要最佳性能");
        log.info("    - 可以手动处理同步");
        
        log.info("\n6. 同步包装:");
        log.info("  ArrayList可以通过Collections.synchronizedList()包装");
        log.info("  但迭代时仍需手动同步，且性能可能不如CopyOnWriteArrayList");
    }
}
