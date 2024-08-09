package com.wx.YX;


import javax.security.auth.login.CredentialException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多任务组合
 *
 * ```java
 * public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs);
 *
 * public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs);
 * ```
 *
 *  allOf：等待所有任务完成
 *
 * anyOf：只要有一个任务完成
 */
public class Demo5 {
    public static void main(String args[]) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName()+"begin....");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int value=1024;
            System.out.println("task1"+value);
            System.out.println(Thread.currentThread().getName()+"end....");
            return value;
        },executorService);

        CompletableFuture<Integer> task2=CompletableFuture.supplyAsync(()->{
            System.out.println(Thread.currentThread().getName()+"begin....");
            int value=200;
            System.out.println("task2"+value);
            System.out.println(Thread.currentThread().getName()+"end....");
            return value;
        },executorService);
        CompletableFuture<Void> completableFuture=CompletableFuture.allOf(task1,task2);
        completableFuture.get();//等待任务全部完成或join
        System.out.println("结束");
    }
}
