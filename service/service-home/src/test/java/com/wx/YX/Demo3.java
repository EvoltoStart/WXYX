package com.wx.YX;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**计算完成时回调方法
//
//当CompletableFuture的计算结果完成，或者抛出异常的时候，可以执行特定的Action。主要是下面的方法：
//whenComplete可以处理正常或异常的计算结果，exceptionally处理异常情况。BiConsumer<? super T,? super Throwable>可以定义处理业务
//
//**whenComplete 和 whenCompleteAsync 的区别：**
//
//whenComplete：是执行当前任务的线程执行继续执行 whenComplete 的任务。  在一个线程
//
//whenCompleteAsync：是执行把 whenCompleteAsync 这个任务继续提交给线程池来进行执行。 可能在一个线程可能多个线程
//
//方法不以Async结尾，意味着Action使用相同的线程执行，而Async可能会使用其他线程执行（如果是使用相同的线程池，也可能会被同一个线程选中执行）
//
//代码示例：
//
*/
public class Demo3 {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        System.out.println("main begin....");
        CompletableFuture<Integer> completableFuture =
                CompletableFuture.supplyAsync(()->{
                            System.out.println("当前线程："+Thread.currentThread().getId());
                            int result = 1024;//10/0;异常情况
                            System.out.println("result:"+result);
                            return result;
                        },executorService)
                        .whenComplete((rs,exception)->{
                            System.out.println("结果："+rs);
                            System.out.println(exception);
                        });
        System.out.println("main over....");
    }
}
