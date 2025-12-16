package com.threadtutorial.collections;

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
public class CopyOnWriteArrayListDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== CopyOnWriteArrayList写时复制列表示例 ===");
        
        System.out.println("\n1. 基本操作示例:");
        basicOperationsExample();
        
        System.out.println("\n2. 线程安全测试 - 读写并发:");
        readWriteConcurrencyExample();
        
        System.out.println("\n3. 迭代器特性 - 快照迭代器:");
        iteratorSnapshotExample();
        
        System.out.println("\n4. 性能特点分析:");
        performanceCharacteristicsExample();
        
        System.out.println("\n5. 适用场景演示:");
        useCaseExample();
        
        System.out.println("\n6. CopyOnWriteArraySet示例:");
        copyOnWriteArraySetExample();
        
        System.out.println("\n7. 与普通ArrayList对比:");
        comparisonWithArrayList();
    }
    
    /**
     * 基本操作示例
     */
    private static void basicOperationsExample() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        
        System.out.println("1) 添加元素:");
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        list.add(1, "Blueberry"); // 在指定位置插入
        System.out.println("添加后: " + list);
        
        System.out.println("\n2) 获取和修改元素:");
        System.out.println("索引1的元素: " + list.get(1));
        list.set(1, "Blackberry");
        System.out.println("修改索引1后: " + list);
        
        System.out.println("\n3) 删除元素:");
        list.remove("Cherry");
        list.remove(0);
        System.out.println("删除后: " + list);
        
        System.out.println("\n4) 其他操作:");
        System.out.println("大小: " + list.size());
        System.out.println("是否为空: " + list.isEmpty());
        System.out.println("包含'Banana': " + list.contains("Banana"));
        System.out.println("索引'Blackberry': " + list.indexOf("Blackberry"));
        
        System.out.println("\n5) 批量操作:");
        list.addAll(List.of("Durian", "Elderberry", "Fig"));
        System.out.println("批量添加后: " + list);
        
        list.removeAll(List.of("Durian", "Fig"));
        System.out.println("批量删除后: " + list);
    }
    
    /**
     * 读写并发测试
     */
    private static void readWriteConcurrencyExample() throws InterruptedException {
        System.out.println("测试CopyOnWriteArrayList的读写并发安全性:");
        
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
        
        System.out.println("读操作次数: " + readCount.get());
        System.out.println("写操作次数: " + writeCount.get());
        System.out.println("最终列表大小: " + list.size());
        System.out.println("测试完成，无并发异常");
    }
    
    /**
     * 迭代器快照特性
     */
    private static void iteratorSnapshotExample() {
        System.out.println("演示CopyOnWriteArrayList迭代器的快照特性:");
        
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        
        System.out.println("原始列表: " + list);
        
        // 获取迭代器（创建快照）
        Iterator<String> iterator = list.iterator();
        
        System.out.println("\n获取迭代器后修改列表:");
        list.add("D");
        list.remove("B");
        System.out.println("修改后列表: " + list);
        
        System.out.println("\n迭代器遍历（显示快照内容）:");
        System.out.print("迭代器内容: ");
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        
        System.out.println("\n\n获取新迭代器:");
        Iterator<String> newIterator = list.iterator();
        System.out.print("新迭代器内容: ");
        while (newIterator.hasNext()) {
            System.out.print(newIterator.next() + " ");
        }
        
        System.out.println("\n\n重要特性：迭代器创建时创建列表的快照副本");
        System.out.println("迭代期间对列表的修改不会影响迭代器");
        System.out.println("这保证了迭代的一致性，但可能看到过时数据");
    }
    
    /**
     * 性能特点分析
     */
    private static void performanceCharacteristicsExample() {
        System.out.println("CopyOnWriteArrayList性能特点分析:");
        
        System.out.println("\n1. 读操作:");
        System.out.println("  - 非常快速，直接访问内部数组");
        System.out.println("  - 不需要同步，支持高并发读");
        System.out.println("  - 时间复杂度: O(1)");
        
        System.out.println("\n2. 写操作:");
        System.out.println("  - 较慢，需要复制整个数组");
        System.out.println("  - 写操作是同步的，但不会阻塞读操作");
        System.out.println("  - 时间复杂度: O(n)");
        
        System.out.println("\n3. 迭代操作:");
        System.out.println("  - 使用快照，迭代期间安全");
        System.out.println("  - 不会抛出ConcurrentModificationException");
        System.out.println("  - 可能迭代到过时数据");
        
        System.out.println("\n4. 内存使用:");
        System.out.println("  - 每次修改都创建新数组，内存开销大");
        System.out.println("  - 适合读多写少的场景");
        
        System.out.println("\n5. 适用场景:");
        System.out.println("  ✓ 读操作远多于写操作");
        System.out.println("  ✓ 需要遍历的安全性和一致性");
        System.out.println("  ✓ 集合大小较小或中等");
        System.out.println("  ✗ 频繁修改的大型集合");
        System.out.println("  ✗ 实时性要求高的场景");
    }
    
    /**
     * 适用场景演示
     */
    private static void useCaseExample() throws InterruptedException {
        System.out.println("CopyOnWriteArrayList适用场景演示:");
        
        // 场景：事件监听器列表（读多写少）
        CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
        
        // 添加监听器（写操作较少）
        listeners.add(() -> System.out.println("监听器1: 事件处理"));
        listeners.add(() -> System.out.println("监听器2: 事件处理"));
        listeners.add(() -> System.out.println("监听器3: 事件处理"));
        
        System.out.println("初始化监听器数量: " + listeners.size());
        
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
        listeners.add(() -> System.out.println("新监听器: 动态添加"));
        
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        
        System.out.println("\n事件处理总数: " + eventCount.get());
        System.out.println("最终监听器数量: " + listeners.size());
        System.out.println("\n场景总结：");
        System.out.println("- 监听器列表读多写少，适合CopyOnWriteArrayList");
        System.out.println("- 事件触发时安全遍历，即使有新的监听器添加");
        System.out.println("- 新监听器对后续事件生效，不影响正在处理的事件");
    }
    
    /**
     * CopyOnWriteArraySet示例
     */
    private static void copyOnWriteArraySetExample() {
        System.out.println("CopyOnWriteArraySet示例（基于CopyOnWriteArrayList）:");
        
        CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
        
        System.out.println("1) 添加元素:");
        set.add("Apple");
        set.add("Banana");
        set.add("Apple"); // 重复元素
        set.add("Cherry");
        System.out.println("添加后: " + set);
        
        System.out.println("\n2) 集合操作:");
        System.out.println("大小: " + set.size());
        System.out.println("是否包含'Banana': " + set.contains("Banana"));
        System.out.println("是否为空: " + set.isEmpty());
        
        System.out.println("\n3) 遍历操作:");
        System.out.print("元素: ");
        for (String fruit : set) {
            System.out.print(fruit + " ");
        }
        
        System.out.println("\n\n4) 线程安全测试:");
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
            e.printStackTrace();
        }
        
        System.out.println("并发操作后集合: " + threadSafeSet);
        System.out.println("无并发异常，线程安全");
    }
    
    /**
     * 与普通ArrayList对比
     */
    private static void comparisonWithArrayList() {
        System.out.println("CopyOnWriteArrayList vs ArrayList对比:");
        
        System.out.println("\n1. 线程安全性:");
        System.out.println("  CopyOnWriteArrayList: 线程安全，适合多线程环境");
        System.out.println("  ArrayList: 非线程安全，需要外部同步");
        
        System.out.println("\n2. 并发修改异常:");
        System.out.println("  CopyOnWriteArrayList: 迭代时修改不会抛异常");
        System.out.println("  ArrayList: 迭代时修改会抛ConcurrentModificationException");
        
        System.out.println("\n3. 性能特点:");
        System.out.println("  CopyOnWriteArrayList: 读快写慢，内存开销大");
        System.out.println("  ArrayList: 读写均衡，内存效率高");
        
        System.out.println("\n4. 迭代器行为:");
        System.out.println("  CopyOnWriteArrayList: 快照迭代器，看到创建时的状态");
        System.out.println("  ArrayList: 快速失败迭代器，检测到修改就失败");
        
        System.out.println("\n5. 使用建议:");
        System.out.println("  使用CopyOnWriteArrayList当:");
        System.out.println("    - 读操作远多于写操作");
        System.out.println("    - 需要遍历时线程安全");
        System.out.println("    - 集合大小较小");
        System.out.println("    - 可以接受数据暂时不一致");
        
        System.out.println("\n  使用ArrayList当:");
        System.out.println("    - 单线程环境");
        System.out.println("    - 读写操作均衡");
        System.out.println("    - 需要最佳性能");
        System.out.println("    - 可以手动处理同步");
        
        System.out.println("\n6. 同步包装:");
        System.out.println("  ArrayList可以通过Collections.synchronizedList()包装");
        System.out.println("  但迭代时仍需手动同步，且性能可能不如CopyOnWriteArrayList");
    }
}
