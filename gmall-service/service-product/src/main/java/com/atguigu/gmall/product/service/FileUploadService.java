package com.atguigu.gmall.product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service
 * class:FileUploadService
 *
 * @author: smile
 * @create: 2023/7/6-14:43
 * @Version: v1.0
 * @Description:
 */
public interface FileUploadService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:上传文件
     */
    String fileUpload(MultipartFile file);
}
