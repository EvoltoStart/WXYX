package com.wx.YX;


/**
 * 线程串行化方法
 *
 * thenApply 方法：当一个线程依赖另一个线程时，获取上一个任务返回的结果，并返回当前任务的返回值。
 *
 * ![img](images\wps3.jpg)
 *
 * thenAccept方法：消费处理结果。接收任务的处理结果，并消费处理，无返回结果。
 *
 * ![img](images\wps4.jpg)
 *
 * thenRun方法：只要上面的任务执行完成，就开始执行thenRun，只是处理完任务后，执行 thenRun的后续操作
 *
 * ![img](images\wps5.jpg)
 *
 * 带有Async默认是异步执行的。这里所谓的异步指的是不在当前线程内执行。
 * */
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo4 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        // 线程1执行返回的结果：100
        CompletableFuture<Integer> futureA =
                CompletableFuture.supplyAsync(() -> {
                    int res = 100;
                    System.out.println("线程一："+res);
                    return res;
                });

        // 线程2 获取到线程1执行的结果
        CompletableFuture<Integer> futureB = futureA.thenApplyAsync((res)->{
            System.out.println("线程二--"+res);
            return ++res;
        },executorService);

        //线程3: 无法获取futureA返回结果
        CompletableFuture<Void> futureC = futureA.thenRunAsync(() -> {
            System.out.println("线程三....");
        }, executorService);
    }
}
