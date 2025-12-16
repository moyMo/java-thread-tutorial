package com.threadtutorial.collections;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟任务类（用于DelayQueue）
 * 实现Delayed接口以支持延迟执行
 */
public class DelayedTask implements Delayed {
    private final String name;
    private final long expiryTime; // 到期时间（毫秒）

    public DelayedTask(String name, long expiryTime) {
        this.name = name;
        this.expiryTime = expiryTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = expiryTime - System.currentTimeMillis();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this) {
            return 0;
        }
        if (other instanceof DelayedTask) {
            DelayedTask otherTask = (DelayedTask) other;
            return Long.compare(this.expiryTime, otherTask.expiryTime);
        }
        long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
        return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    @Override
    public String toString() {
        return name + " (到期时间: " + expiryTime + ")";
    }

    // Getter方法（可选）
    public String getName() {
        return name;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}
