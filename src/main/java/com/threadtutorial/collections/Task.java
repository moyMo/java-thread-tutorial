package com.threadtutorial.collections;

/**
 * 任务类（用于PriorityBlockingQueue）
 * 实现Comparable接口以支持优先级排序
 */
public class Task implements Comparable<Task> {
    private final String name;
    private final int priority; // 优先级，数字越小优先级越高

    public Task(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return name + " (优先级: " + priority + ")";
    }

    // Getter方法（可选）
    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }
}
