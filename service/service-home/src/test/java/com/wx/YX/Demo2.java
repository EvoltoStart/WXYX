package com.wx.YX;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo2 {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        System.out.println("main begin....");
        CompletableFuture<Integer> completableFuture =
                CompletableFuture.supplyAsync(()->{
                    System.out.println("当前线程："+Thread.currentThread().getId());
                    int result = 1024;
                    System.out.println("result:"+result);
                    return result;
                },executorService);
        //获取返回结果
        Integer value = completableFuture.get();//阻塞等待
        System.out.println("main over...."+value);
    }
}
