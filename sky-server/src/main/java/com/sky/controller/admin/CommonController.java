package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("upload")
    public Result<String> upload(MultipartFile file) {
        try {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = UUID.nameUUIDFromBytes(originalFilename.getBytes()).toString() + extension;
        String path=aliOssUtil.upload(file.getBytes(), objectName);
        return Result.success(path);
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}
