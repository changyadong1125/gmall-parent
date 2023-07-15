package com.atguigu.gmall.product.demo;

import springfox.bean.validators.plugins.schema.MinMaxAnnotationPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.demo
 * class:CompletableFutureDemo
 *
 * @author: smile
 * @create: 2023/7/15-14:18
 * @Version: v1.0
 * @Description:
 */
public class CompletableFutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> System.out.println("不是太好写啊"));
//        System.out.println(completableFuture.get());
//
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//                    System.out.println("有返回值");
//                    return "404";
//                })//t 表示上一个返回结果  u表示是否有异常处理
//                .whenComplete((t, u) -> System.out.println("t" + t + u));
//        System.out.println(future.get())
//        .thenRun(new Runnable() {
//                    @Override
//                    public void run() {
//                        System.out.println("args thenRun = " + args);
//                    }
//                });


//        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
//                    System.out.println("有返回值");
////                    int a= 9/0;
//                    return "404";
//                }).thenAccept(s -> System.out.println("s  thenAccept = " + s))
//
//                .thenApply((a) -> {
//                    System.out.println("a  thenApply = " + a);
//                    return a + "000000000";
//                })
//
//                //完成时回调 获取结果 和异常
//                .whenCompleteAsync((t, u) -> {
//                    System.out.println("t whenCompleteAsync = " + t);
//                    System.out.println("u whenCompleteAsync = " + u);
//                })
////                计算完成时回调处理异常
//                .exceptionally(t ->
//                {
//                    System.out.println("t  exceptionally = " + t);
//                    return "string";
//                }).thenApply(s -> {
//                            System.out.println("s  thenApply = " + s);
//                            return s + 99999999;
//                        }
//                );
//        System.out.println("future1.get() = " + future1.get());


        //创建线程a
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "hello");

        CompletableFuture<Void> futureB = futureA.thenAcceptAsync(A -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(A + "B");
                }
        );

        CompletableFuture<Void> futureC = futureA.thenAcceptAsync(A -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(A + "C");
                }
        );
        System.out.println("futureB = " + futureB.get());
        System.out.println("futureC = " + futureC.get());
    }
}
