package com.threadtutorial.projects;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 银行转账模拟系统
 * 演示多线程环境下的账户安全和并发控制
 */
public class BankTransferSimulation {

    // 银行类
    static class Bank {
        private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
        private final AtomicInteger transactionId = new AtomicInteger(0);
        private final AtomicInteger successfulTransactions = new AtomicInteger(0);
        private final AtomicInteger failedTransactions = new AtomicInteger(0);
        
        /**
         * 创建账户
         */
        public Account createAccount(int accountId, String owner, double initialBalance) {
            Account account = new Account(accountId, owner, initialBalance);
            accounts.put(accountId, account);
            return account;
        }
        
        /**
         * 转账方法（使用synchronized）
         */
        public boolean transferSync(int fromId, int toId, double amount) {
            Account from = accounts.get(fromId);
            Account to = accounts.get(toId);
            
            if (from == null || to == null) {
                System.err.println("账户不存在");
                failedTransactions.incrementAndGet();
                return false;
            }
            
            // 对两个账户加锁，避免死锁（按ID顺序加锁）
            Account first = fromId < toId ? from : to;
            Account second = fromId < toId ? to : from;
            
            synchronized (first) {
                synchronized (second) {
                    return performTransfer(from, to, amount);
                }
            }
        }
        
        /**
         * 转账方法（使用ReentrantLock）
         */
        public boolean transferWithLock(int fromId, int toId, double amount) {
            Account from = accounts.get(fromId);
            Account to = accounts.get(toId);
            
            if (from == null || to == null) {
                System.err.println("账户不存在");
                failedTransactions.incrementAndGet();
                return false;
            }
            
            // 尝试获取两个锁，避免死锁
            while (true) {
                if (from.lock.tryLock()) {
                    try {
                        if (to.lock.tryLock()) {
                            try {
                                return performTransfer(from, to, amount);
                            } finally {
                                to.lock.unlock();
                            }
                        }
                    } finally {
                        from.lock.unlock();
                    }
                }
                
                // 获取锁失败，稍后重试
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        /**
         * 执行转账
         */
        private boolean performTransfer(Account from, Account to, double amount) {
            int tid = transactionId.incrementAndGet();
            
            if (from.getBalance() < amount) {
                System.out.println("[" + tid + "] 转账失败: " + from.owner + " -> " + 
                                 to.owner + " 金额: " + amount + " (余额不足)");
                failedTransactions.incrementAndGet();
                return false;
            }
            
            // 执行转账
            from.withdraw(amount);
            to.deposit(amount);
            
            System.out.println("[" + tid + "] 转账成功: " + from.owner + " -> " + 
                             to.owner + " 金额: " + amount + 
                             " 余额: " + from.getBalance() + " -> " + to.getBalance());
            successfulTransactions.incrementAndGet();
            return true;
        }
        
        /**
         * 获取账户
         */
        public Account getAccount(int accountId) {
            return accounts.get(accountId);
        }
        
        /**
         * 打印银行状态
         */
        public void printBankStatus() {
            System.out.println("\n=== 银行状态 ===");
            System.out.println("总账户数: " + accounts.size());
            System.out.println("成功交易: " + successfulTransactions.get());
            System.out.println("失败交易: " + failedTransactions.get());
            System.out.println("总交易: " + transactionId.get());
            System.out.println("成功率: " + 
                (transactionId.get() > 0 ? 
                 (successfulTransactions.get() * 100.0 / transactionId.get()) : 0) + "%");
            
            double totalBalance = 0;
            for (Account account : accounts.values()) {
                totalBalance += account.getBalance();
                System.out.println("账户 " + account.accountId + " (" + account.owner + 
                                 "): 余额 = " + account.getBalance());
            }
            System.out.println("银行总资产: " + totalBalance);
        }
    }
    
    /**
     * 账户类
     */
    static class Account {
        final int accountId;
        final String owner;
        private double balance;
        final Lock lock = new ReentrantLock();
        
        public Account(int accountId, String owner, double initialBalance) {
            this.accountId = accountId;
            this.owner = owner;
            this.balance = initialBalance;
        }
        
        public double getBalance() {
            return balance;
        }
        
        public void deposit(double amount) {
            if (amount > 0) {
                balance += amount;
            }
        }
        
        public boolean withdraw(double amount) {
            if (amount > 0 && balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }
    }
    
    /**
     * 转账任务
     */
    static class TransferTask implements Runnable {
        private final Bank bank;
        private final int fromId;
        private final int toId;
        private final double amount;
        private final boolean useLock;
        
        public TransferTask(Bank bank, int fromId, int toId, double amount, boolean useLock) {
            this.bank = bank;
            this.fromId = fromId;
            this.toId = toId;
            this.amount = amount;
            this.useLock = useLock;
        }
        
        @Override
        public void run() {
            if (useLock) {
                bank.transferWithLock(fromId, toId, amount);
            } else {
                bank.transferSync(fromId, toId, amount);
            }
        }
    }
    
    /**
     * 主方法 - 演示使用
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 银行转账模拟系统 ===");
        
        // 创建银行
        Bank bank = new Bank();
        
        // 创建账户
        System.out.println("\n1. 创建账户:");
        Account alice = bank.createAccount(1, "Alice", 10000);
        Account bob = bank.createAccount(2, "Bob", 5000);
        Account charlie = bank.createAccount(3, "Charlie", 8000);
        Account david = bank.createAccount(4, "David", 3000);
        
        System.out.println("初始账户状态:");
        bank.printBankStatus();
        
        // 测试1: 使用synchronized的转账
        System.out.println("\n2. 测试synchronized转账:");
        testSynchronizedTransfers(bank);
        
        // 重置银行状态
        resetBank(bank);
        
        // 测试2: 使用ReentrantLock的转账
        System.out.println("\n3. 测试ReentrantLock转账:");
        testLockTransfers(bank);
        
        // 测试3: 并发转账测试
        System.out.println("\n4. 高并发转账测试:");
        testConcurrentTransfers(bank);
        
        System.out.println("\n=== 技术要点总结 ===");
        System.out.println("1. 线程安全问题:");
        System.out.println("   - 共享资源（账户余额）需要同步访问");
        System.out.println("   - 转账涉及多个账户，需要原子性操作");
        
        System.out.println("\n2. 同步方法对比:");
        System.out.println("   synchronized: 简单易用，自动释放锁");
        System.out.println("   ReentrantLock: 更灵活，支持尝试锁、超时等");
        
        System.out.println("\n3. 死锁预防:");
        System.out.println("   - 按固定顺序获取锁（账户ID顺序）");
        System.out.println("   - 使用tryLock()避免无限等待");
        System.out.println("   - 设置锁获取超时时间");
        
        System.out.println("\n4. 性能考虑:");
        System.out.println("   - 锁粒度要适当，避免过度同步");
        System.out.println("   - 考虑使用读写锁（ReadWriteLock）");
        System.out.println("   - 对于高并发场景，考虑无锁算法");
    }
    
    /**
     * 测试synchronized转账
     */
    private static void testSynchronizedTransfers(Bank bank) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 提交转账任务
        for (int i = 0; i < 10; i++) {
            executor.submit(new TransferTask(bank, 1, 2, 100, false)); // Alice -> Bob
            executor.submit(new TransferTask(bank, 2, 3, 50, false));  // Bob -> Charlie
            executor.submit(new TransferTask(bank, 3, 4, 80, false));  // Charlie -> David
            executor.submit(new TransferTask(bank, 4, 1, 30, false));  // David -> Alice
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        bank.printBankStatus();
    }
    
    /**
     * 测试ReentrantLock转账
     */
    private static void testLockTransfers(Bank bank) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 提交转账任务
        for (int i = 0; i < 10; i++) {
            executor.submit(new TransferTask(bank, 1, 2, 100, true)); // Alice -> Bob
            executor.submit(new TransferTask(bank, 2, 3, 50, true));  // Bob -> Charlie
            executor.submit(new TransferTask(bank, 3, 4, 80, true));  // Charlie -> David
            executor.submit(new TransferTask(bank, 4, 1, 30, true));  // David -> Alice
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        bank.printBankStatus();
    }
    
    /**
     * 测试高并发转账
     */
    private static void testConcurrentTransfers(Bank bank) throws InterruptedException {
        System.out.println("启动100个并发转账任务...");
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<?>> futures = new ArrayList<>();
        
        // 提交大量转账任务
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int from = random.nextInt(4) + 1;
            int to;
            do {
                to = random.nextInt(4) + 1;
            } while (to == from);
            
            double amount = 10 + random.nextInt(100);
            boolean useLock = random.nextBoolean();
            
            futures.add(executor.submit(new TransferTask(bank, from, to, amount, useLock)));
        }
        
        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                System.err.println("任务执行异常: " + e.getMessage());
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        bank.printBankStatus();
    }
    
    /**
     * 重置银行状态
     */
    private static void resetBank(Bank bank) {
        // 重置账户余额
        bank.getAccount(1).deposit(10000 - bank.getAccount(1).getBalance());
        bank.getAccount(2).deposit(5000 - bank.getAccount(2).getBalance());
        bank.getAccount(3).deposit(8000 - bank.getAccount(3).getBalance());
        bank.getAccount(4).deposit(3000 - bank.getAccount(4).getBalance());
    }
    
    /**
     * 高级特性演示
     */
    public static void advancedDemo() {
        System.out.println("\n=== 高级特性演示 ===");
        
        System.out.println("1. 分布式锁:");
        System.out.println("   - 使用Redis或ZooKeeper实现分布式锁");
        System.out.println("   - 支持跨JVM的账户同步");
        System.out.println("   - 实现分布式事务");
        
        System.out.println("\n2. 事务管理:");
        System.out.println("   - 实现转账事务的ACID特性");
        System.out.println("   - 支持事务回滚");
        System.out.println("   - 实现事务日志");
        
        System.out.println("\n3. 性能优化:");
        System.out.println("   - 使用分段锁（Striped Lock）");
        System.out.println("   - 实现无锁数据结构");
        System.out.println("   - 使用CAS操作");
        
        System.out.println("\n4. 监控和告警:");
        System.out.println("   - 实时监控账户余额");
        System.out.println("   - 检测异常交易模式");
        System.out.println("   - 实现交易审计");
        
        System.out.println("\n5. 扩展功能:");
        System.out.println("   - 支持多种货币");
        System.out.println("   - 实现汇率转换");
        System.out.println("   - 支持批量转账");
        System.out.println("   - 实现利息计算");
        
        System.out.println("\n6. 安全考虑:");
        System.out.println("   - 实现身份验证和授权");
        System.out.println("   - 加密敏感数据");
        System.out.println("   - 防止SQL注入和XSS攻击");
        System.out.println("   - 实现防欺诈检测");
    }
    
    /**
     * 死锁演示和解决方案
     */
    public static void deadlockDemo() {
        System.out.println("\n=== 死锁演示和解决方案 ===");
        
        System.out.println("1. 死锁场景:");
        System.out.println("   线程1: 锁A -> 等待锁B");
        System.out.println("   线程2: 锁B -> 等待锁A");
        
        System.out.println("\n2. 死锁预防策略:");
        System.out.println("   a) 锁顺序: 总是按相同顺序获取锁");
        System.out.println("   b) 超时机制: 设置锁获取超时时间");
        System.out.println("   c) 死锁检测: 定期检查死锁并恢复");
        
        System.out.println("\n3. 实际代码示例:");
        System.out.println("   // 错误的锁顺序（可能导致死锁）");
        System.out.println("   synchronized(account1) {");
        System.out.println("     synchronized(account2) { ... }");
        System.out.println("   }");
        
        System.out.println("\n   // 正确的锁顺序（避免死锁）");
        System.out.println("   Account first = account1.id < account2.id ? account1 : account2;");
        System.out.println("   Account second = account1.id < account2.id ? account2 : account1;");
        System.out.println("   synchronized(first) {");
        System.out.println("     synchronized(second) { ... }");
        System.out.println("   }");
        
        System.out.println("\n4. 使用tryLock避免死锁:");
        System.out.println("   if (lock1.tryLock()) {");
        System.out.println("     try {");
        System.out.println("       if (lock2.tryLock()) {");
        System.out.println("         try { ... } finally { lock2.unlock(); }");
        System.out.println("       }");
        System.out.println("     } finally { lock1.unlock(); }");
        System.out.println("   }");
    }
}
