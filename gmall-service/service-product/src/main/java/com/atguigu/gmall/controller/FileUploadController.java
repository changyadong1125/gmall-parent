package com.atguigu.gmall.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.service.FileUploadService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.controller
 * class:FileUploadController
 *
 * @author: smile
 * @create: 2023/7/6-14:36
 * @Version: v1.0
 * @Description:
 */
@Api("文件上传")
@RestController
@RequestMapping("/admin/product")
public class FileUploadController {
    @Resource
    private FileUploadService fileUploadService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:文件上传
     * springMVC封装好的MultiPartFile
     */
    @PostMapping("/fileUpload")
    public Result<String> fileUpload(MultipartFile file){
        String fileUrl = fileUploadService.fileUpload(file);
        return Result.ok(fileUrl);
    }
}
