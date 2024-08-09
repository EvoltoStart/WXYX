package com.wx.YX;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo1 {
    public static void main(String[] args) {
        //创建线程池
        ExecutorService executorService=Executors.newFixedThreadPool(10);
        //CompletableFuture创建异步对象
        CompletableFuture<Void> completableFuture=CompletableFuture.runAsync(()->{
            System.out.println("当前线程:"+Thread.currentThread().getName());
        },executorService);
        System.out.println("main over...");
    }

}
