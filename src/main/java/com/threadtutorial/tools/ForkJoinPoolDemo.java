package com.threadtutorial.tools;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * ForkJoinPool分治线程池示例
 * 演示Java 7+的Fork/Join框架，适用于分治算法和并行计算
 */
public class ForkJoinPoolDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== ForkJoinPool分治线程池示例 ===");
        
        System.out.println("\n1. 计算斐波那契数列 (RecursiveTask):");
        fibonacciExample();
        
        System.out.println("\n2. 数组求和 (RecursiveTask):");
        arraySumExample();
        
        System.out.println("\n3. 数组排序 (RecursiveAction):");
        arraySortExample();
        
        System.out.println("\n4. 文件搜索 (RecursiveTask):");
        fileSearchExample();
        
        System.out.println("\n5. 并行流与ForkJoinPool:");
        parallelStreamExample();
        
        System.out.println("\n6. 自定义ForkJoinPool配置:");
        customForkJoinPoolExample();
    }
    
    /**
     * 斐波那契数列示例
     */
    private static void fibonacciExample() {
        int n = 20; // 计算第20个斐波那契数
        
        // 创建ForkJoinPool
        ForkJoinPool pool = new ForkJoinPool();
        
        // 创建任务
        FibonacciTask task = new FibonacciTask(n);
        
        // 提交任务并获取结果
        long startTime = System.currentTimeMillis();
        long result = pool.invoke(task);
        long endTime = System.currentTimeMillis();
        
        System.out.println("斐波那契数列第 " + n + " 项: " + result);
        System.out.println("计算耗时: " + (endTime - startTime) + "ms");
        System.out.println("ForkJoinPool状态: " + pool);
        
        // 关闭线程池
        pool.shutdown();
    }
    
    /**
     * 数组求和示例
     */
    private static void arraySumExample() {
        // 创建一个大数组
        int[] array = new int[1000000];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(100);
        }
        
        // 使用ForkJoinPool计算数组和
        ForkJoinPool pool = new ForkJoinPool();
        ArraySumTask task = new ArraySumTask(array, 0, array.length);
        
        long startTime = System.currentTimeMillis();
        long sum = pool.invoke(task);
        long endTime = System.currentTimeMillis();
        
        System.out.println("数组大小: " + array.length);
        System.out.println("数组总和: " + sum);
        System.out.println("并行计算耗时: " + (endTime - startTime) + "ms");
        
        // 验证结果（使用串行计算）
        long serialSum = 0;
        startTime = System.currentTimeMillis();
        for (int value : array) {
            serialSum += value;
        }
        endTime = System.currentTimeMillis();
        
        System.out.println("串行计算总和: " + serialSum);
        System.out.println("串行计算耗时: " + (endTime - startTime) + "ms");
        System.out.println("结果验证: " + (sum == serialSum ? "正确" : "错误"));
        
        pool.shutdown();
    }
    
    /**
     * 数组排序示例
     */
    private static void arraySortExample() {
        // 创建一个大数组
        int[] array = new int[10000];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(10000);
        }
        
        // 复制数组用于验证
        int[] arrayCopy = array.clone();
        
        // 使用ForkJoinPool并行排序
        ForkJoinPool pool = new ForkJoinPool();
        ArraySortAction task = new ArraySortAction(array, 0, array.length);
        
        long startTime = System.currentTimeMillis();
        pool.invoke(task);
        long endTime = System.currentTimeMillis();
        
        System.out.println("数组大小: " + array.length);
        System.out.println("并行排序耗时: " + (endTime - startTime) + "ms");
        
        // 验证排序结果
        boolean sorted = true;
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                sorted = false;
                break;
            }
        }
        System.out.println("排序验证: " + (sorted ? "成功" : "失败"));
        
        // 串行排序对比
        startTime = System.currentTimeMillis();
        Arrays.sort(arrayCopy);
        endTime = System.currentTimeMillis();
        System.out.println("串行排序耗时: " + (endTime - startTime) + "ms");
        
        pool.shutdown();
    }
    
    /**
     * 文件搜索示例
     */
    private static void fileSearchExample() {
        // 模拟文件系统结构
        List<String> fileSystem = Arrays.asList(
            "/home/user/documents/report.pdf",
            "/home/user/documents/invoice.docx",
            "/home/user/images/photo1.jpg",
            "/home/user/images/photo2.jpg",
            "/home/user/code/Project1/src/Main.java",
            "/home/user/code/Project1/src/Utils.java",
            "/home/user/code/Project2/src/App.java",
            "/home/user/music/song1.mp3",
            "/home/user/music/song2.mp3"
        );
        
        String searchPattern = ".java"; // 搜索Java文件
        
        ForkJoinPool pool = new ForkJoinPool();
        FileSearchTask task = new FileSearchTask(fileSystem, searchPattern, 0, fileSystem.size());
        
        long startTime = System.currentTimeMillis();
        List<String> results = pool.invoke(task);
        long endTime = System.currentTimeMillis();
        
        System.out.println("搜索模式: *" + searchPattern);
        System.out.println("文件总数: " + fileSystem.size());
        System.out.println("找到文件数: " + results.size());
        System.out.println("搜索耗时: " + (endTime - startTime) + "ms");
        
        if (!results.isEmpty()) {
            System.out.println("找到的文件:");
            results.forEach(file -> System.out.println("  - " + file));
        }
        
        pool.shutdown();
    }
    
    /**
     * 并行流示例
     */
    private static void parallelStreamExample() {
        System.out.println("演示并行流如何使用ForkJoinPool:");
        
        // 创建一个大列表
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 1000000; i++) {
            numbers.add(i);
        }
        
        // 使用并行流计算
        long startTime = System.currentTimeMillis();
        long sum = numbers.parallelStream()
                         .mapToLong(Integer::longValue)
                         .sum();
        long endTime = System.currentTimeMillis();
        
        System.out.println("列表大小: " + numbers.size());
        System.out.println("总和: " + sum);
        System.out.println("并行流计算耗时: " + (endTime - startTime) + "ms");
        
        // 使用自定义ForkJoinPool
        System.out.println("\n使用自定义ForkJoinPool执行并行流:");
        ForkJoinPool customPool = new ForkJoinPool(4); // 4个线程
        
        startTime = System.currentTimeMillis();
        long customSum = customPool.submit(() -> 
            numbers.parallelStream()
                   .mapToLong(Integer::longValue)
                   .sum()
        ).join();
        endTime = System.currentTimeMillis();
        
        System.out.println("自定义线程池计算总和: " + customSum);
        System.out.println("自定义线程池计算耗时: " + (endTime - startTime) + "ms");
        
        customPool.shutdown();
    }
    
    /**
     * 自定义ForkJoinPool配置示例
     */
    private static void customForkJoinPoolExample() throws InterruptedException {
        System.out.println("自定义ForkJoinPool配置:");
        
        // 创建自定义配置的ForkJoinPool
        ForkJoinPool customPool = new ForkJoinPool(
            4, // 并行级别（线程数）
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            (t, e) -> System.err.println("未捕获异常: " + e.getMessage()), // 异常处理器
            true // 异步模式
        );
        
        System.out.println("自定义线程池配置:");
        System.out.println("并行级别: " + customPool.getParallelism());
        System.out.println("线程池大小: " + customPool.getPoolSize());
        System.out.println("活跃线程数: " + customPool.getActiveThreadCount());
        System.out.println("窃取任务数: " + customPool.getStealCount());
        
        // 提交多个任务
        List<ForkJoinTask<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            ForkJoinTask<Long> task = customPool.submit(() -> {
                System.out.println("任务 " + taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                long result = 0;
                for (int j = 0; j < 1000000; j++) {
                    result += j;
                }
                return result;
            });
            tasks.add(task);
        }
        
        // 获取结果
        for (int i = 0; i < tasks.size(); i++) {
            try {
                Long result = tasks.get(i).get();
                System.out.println("任务 " + i + " 结果: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("\n最终线程池状态:");
        System.out.println("线程池大小: " + customPool.getPoolSize());
        System.out.println("活跃线程数: " + customPool.getActiveThreadCount());
        System.out.println("窃取任务数: " + customPool.getStealCount());
        System.out.println("队列任务数: " + customPool.getQueuedTaskCount());
        
        // 优雅关闭
        customPool.shutdown();
        customPool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("自定义ForkJoinPool示例完成");
    }
    
    /**
     * 斐波那契数列计算任务 (RecursiveTask)
     */
    static class FibonacciTask extends RecursiveTask<Long> {
        private final int n;
        
        public FibonacciTask(int n) {
            this.n = n;
        }
        
        @Override
        protected Long compute() {
            if (n <= 1) {
                return (long) n;
            }
            
            // 如果n较小，直接计算
            if (n <= 10) {
                return computeDirectly();
            }
            
            // 否则分解任务
            FibonacciTask task1 = new FibonacciTask(n - 1);
            FibonacciTask task2 = new FibonacciTask(n - 2);
            
            task1.fork(); // 异步执行
            Long result2 = task2.compute(); // 同步执行
            Long result1 = task1.join(); // 等待结果
            
            return result1 + result2;
        }
        
        private Long computeDirectly() {
            if (n <= 1) return (long) n;
            
            long a = 0, b = 1;
            for (int i = 2; i <= n; i++) {
                long temp = a + b;
                a = b;
                b = temp;
            }
            return b;
        }
    }
    
    /**
     * 数组求和任务 (RecursiveTask)
     */
    static class ArraySumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10000; // 阈值
        private final int[] array;
        private final int start;
        private final int end;
        
        public ArraySumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Long compute() {
            int length = end - start;
            
            // 如果小于阈值，直接计算
            if (length <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }
            
            // 否则分解任务
            int middle = start + (length / 2);
            ArraySumTask leftTask = new ArraySumTask(array, start, middle);
            ArraySumTask rightTask = new ArraySumTask(array, middle, end);
            
            leftTask.fork(); // 异步执行左半部分
            Long rightResult = rightTask.compute(); // 同步执行右半部分
            Long leftResult = leftTask.join(); // 等待左半部分结果
            
            return leftResult + rightResult;
        }
    }
    
    /**
     * 数组排序任务 (RecursiveAction)
     */
    static class ArraySortAction extends RecursiveAction {
        private static final int THRESHOLD = 1000; // 阈值
        private final int[] array;
        private final int start;
        private final int end;
        
        public ArraySortAction(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected void compute() {
            int length = end - start;
            
            // 如果小于阈值，直接排序
            if (length <= THRESHOLD) {
                Arrays.sort(array, start, end);
                return;
            }
            
            // 否则分解任务
            int middle = start + (length / 2);
            ArraySortAction leftTask = new ArraySortAction(array, start, middle);
            ArraySortAction rightTask = new ArraySortAction(array, middle, end);
            
            invokeAll(leftTask, rightTask); // 并行执行两个任务
            
            // 合并结果
            merge(array, start, middle, end);
        }
        
        private void merge(int[] array, int start, int middle, int end) {
            int[] temp = new int[end - start];
            int i = start, j = middle, k = 0;
            
            while (i < middle && j < end) {
                if (array[i] <= array[j]) {
                    temp[k++] = array[i++];
                } else {
                    temp[k++] = array[j++];
                }
            }
            
            while (i < middle) {
                temp[k++] = array[i++];
            }
            
            while (j < end) {
                temp[k++] = array[j++];
            }
            
            System.arraycopy(temp, 0, array, start, temp.length);
        }
    }
    
    /**
     * 文件搜索任务 (RecursiveTask)
     */
    static class FileSearchTask extends RecursiveTask<List<String>> {
        private static final int THRESHOLD = 3; // 阈值
        private final List<String> files;
        private final String pattern;
        private final int start;
        private final int end;
        
        public FileSearchTask(List<String> files, String pattern, int start, int end) {
            this.files = files;
            this.pattern = pattern;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected List<String> compute() {
            int length = end - start;
            
            // 如果小于阈值，直接搜索
            if (length <= THRESHOLD) {
                List<String> results = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    String file = files.get(i);
                    if (file.contains(pattern)) {
                        results.add(file);
                    }
                }
                return results;
            }
            
            // 否则分解任务
            int middle = start + (length / 2);
            FileSearchTask leftTask = new FileSearchTask(files, pattern, start, middle);
            FileSearchTask rightTask = new FileSearchTask(files, pattern, middle, end);
            
            leftTask.fork(); // 异步执行左半部分
            List<String> rightResult = rightTask.compute(); // 同步执行右半部分
            List<String> leftResult = leftTask.join(); // 等待左半部分结果
            
            // 合并结果
            leftResult.addAll(rightResult);
            return leftResult;
        }
    }
}
