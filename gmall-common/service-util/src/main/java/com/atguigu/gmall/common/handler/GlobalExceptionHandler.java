package com.atguigu.gmall.common.handler;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 */
@ControllerAdvice
@Slf4j
@ResponseBody
public class GlobalExceptionHandler {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description: 运行时异常全局处理
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> exceptionError(RuntimeException e) {
        log.error("发生运行时异常RuntimeException: {}", e.getMessage(), e);
        return Result.fail(e.getMessage());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description: 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<?> exceptionError(Exception e) {
        log.error("发生全局异常Exception: {}", e.getMessage(), e);
        return Result.fail(e.getMessage());
    }
//    @ExceptionHandler(Exception.class)
//    @ResponseBody
//    public Result error(Exception e){
//        e.printStackTrace();
//        return Result.fail();
//    }
//
//    /**
//     * 自定义异常处理方法
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(GmallException.class)
//    @ResponseBody
//    public Result error(GmallException e){
//        return Result.fail(e.getMessage());
//    }
}
