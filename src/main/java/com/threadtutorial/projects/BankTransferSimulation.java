package com.threadtutorial.projects;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 银行转账模拟系统
 * 演示多线程环境下的账户安全和并发控制
 */
@Slf4j
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
                log.info("[ {}", tid + "] 转账失败: " + from.owner + " -> " + 
                                 to.owner + " 金额: " + amount + " (余额不足)");
                failedTransactions.incrementAndGet();
                return false;
            }
            
            // 执行转账
            from.withdraw(amount);
            to.deposit(amount);
            
            log.info("[ {}", tid + "] 转账成功: " + from.owner + " -> " + 
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
            log.info("\n=== 银行状态 ===");
            log.info("总账户数:  {}", accounts.size());
            log.info("成功交易:  {}", successfulTransactions.get());
            log.info("失败交易:  {}", failedTransactions.get());
            log.info("总交易:  {}", transactionId.get());
            log.info("成功率:  {}", (transactionId.get() > 0 ? 
                 (successfulTransactions.get() * 100.0 / transactionId.get()) : 0) + "%");
            
            double totalBalance = 0;
            for (Account account : accounts.values()) {
                totalBalance += account.getBalance();
                log.info("账户  {}", account.accountId + " (" + account.owner + 
                                 "): 余额 = " + account.getBalance());
            }
            log.info("银行总资产:  {}", totalBalance);
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
        log.info("=== 银行转账模拟系统 ===");
        
        // 创建银行
        Bank bank = new Bank();
        
        // 创建账户
        log.info("\n1. 创建账户:");
        Account alice = bank.createAccount(1, "Alice", 10000);
        Account bob = bank.createAccount(2, "Bob", 5000);
        Account charlie = bank.createAccount(3, "Charlie", 8000);
        Account david = bank.createAccount(4, "David", 3000);
        
        log.info("初始账户状态:");
        bank.printBankStatus();
        
        // 测试1: 使用synchronized的转账
        log.info("\n2. 测试synchronized转账:");
        testSynchronizedTransfers(bank);
        
        // 重置银行状态
        resetBank(bank);
        
        // 测试2: 使用ReentrantLock的转账
        log.info("\n3. 测试ReentrantLock转账:");
        testLockTransfers(bank);
        
        // 测试3: 并发转账测试
        log.info("\n4. 高并发转账测试:");
        testConcurrentTransfers(bank);
        
        log.info("\n=== 技术要点总结 ===");
        log.info("1. 线程安全问题:");
        log.info("   - 共享资源（账户余额）需要同步访问");
        log.info("   - 转账涉及多个账户，需要原子性操作");
        
        log.info("\n2. 同步方法对比:");
        log.info("   synchronized: 简单易用，自动释放锁");
        log.info("   ReentrantLock: 更灵活，支持尝试锁、超时等");
        
        log.info("\n3. 死锁预防:");
        log.info("   - 按固定顺序获取锁（账户ID顺序）");
        log.info("   - 使用tryLock()避免无限等待");
        log.info("   - 设置锁获取超时时间");
        
        log.info("\n4. 性能考虑:");
        log.info("   - 锁粒度要适当，避免过度同步");
        log.info("   - 考虑使用读写锁（ReadWriteLock）");
        log.info("   - 对于高并发场景，考虑无锁算法");
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
        log.info("启动100个并发转账任务...");
        
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
        log.info("\n=== 高级特性演示 ===");
        
        log.info("1. 分布式锁:");
        log.info("   - 使用Redis或ZooKeeper实现分布式锁");
        log.info("   - 支持跨JVM的账户同步");
        log.info("   - 实现分布式事务");
        
        log.info("\n2. 事务管理:");
        log.info("   - 实现转账事务的ACID特性");
        log.info("   - 支持事务回滚");
        log.info("   - 实现事务日志");
        
        log.info("\n3. 性能优化:");
        log.info("   - 使用分段锁（Striped Lock）");
        log.info("   - 实现无锁数据结构");
        log.info("   - 使用CAS操作");
        
        log.info("\n4. 监控和告警:");
        log.info("   - 实时监控账户余额");
        log.info("   - 检测异常交易模式");
        log.info("   - 实现交易审计");
        
        log.info("\n5. 扩展功能:");
        log.info("   - 支持多种货币");
        log.info("   - 实现汇率转换");
        log.info("   - 支持批量转账");
        log.info("   - 实现利息计算");
        
        log.info("\n6. 安全考虑:");
        log.info("   - 实现身份验证和授权");
        log.info("   - 加密敏感数据");
        log.info("   - 防止SQL注入和XSS攻击");
        log.info("   - 实现防欺诈检测");
    }
    
    /**
     * 死锁演示和解决方案
     */
    public static void deadlockDemo() {
        log.info("\n=== 死锁演示和解决方案 ===");
        
        log.info("1. 死锁场景:");
        log.info("   线程1: 锁A -> 等待锁B");
        log.info("   线程2: 锁B -> 等待锁A");
        
        log.info("\n2. 死锁预防策略:");
        log.info("   a) 锁顺序: 总是按相同顺序获取锁");
        log.info("   b) 超时机制: 设置锁获取超时时间");
        log.info("   c) 死锁检测: 定期检查死锁并恢复");
        
        log.info("\n3. 实际代码示例:");
        log.info("   // 错误的锁顺序（可能导致死锁）");
        log.info("   synchronized(account1) {");
        log.info("     synchronized(account2) { ... }");
        log.info("   }");
        
        log.info("\n   // 正确的锁顺序（避免死锁）");
        log.info("   Account first = account1.id < account2.id ? account1 : account2;");
        log.info("   Account second = account1.id < account2.id ? account2 : account1;");
        log.info("   synchronized(first) {");
        log.info("     synchronized(second) { ... }");
        log.info("   }");
        
        log.info("\n4. 使用tryLock避免死锁:");
        log.info("   if (lock1.tryLock()) {");
        log.info("     try {");
        log.info("       if (lock2.tryLock()) {");
        log.info("         try { ... } finally { lock2.unlock(); }");
        log.info("       }");
        log.info("     } finally { lock1.unlock(); }");
        log.info("   }");
    }
}
