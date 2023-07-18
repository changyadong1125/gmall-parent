package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.FileUploadService;
import io.minio.*;
import io.minio.errors.MinioException;
import jodd.io.FileNameUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service.impl
 * class:FileUploadServiceImp
 *
 * @author: smile
 * @create: 2023/7/6-14:44
 * @Version: v1.0
 * @Description:
 */
@Service
@RefreshScope
public class FileUploadServiceImp implements FileUploadService {

    @Value("${minio.endpointUrl}")
    private String endpointUrl;
    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secreKey}")
    private String secreKey;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:文件上传
     */
    @Override
    public String fileUpload(MultipartFile file) {

        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpointUrl)
                            .credentials(accessKey, secreKey)
                            .build();

            // Make 'asiatrip' bucket if not exist.
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("Bucket 'gmall' already exists.");
            }

            // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + FileNameUtil.getExtension(file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return endpointUrl + "/" + bucketName + "/" + fileName;
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
