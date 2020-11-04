package com.aricas.convert.controller;

import com.aricas.convert.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Api(value = "文件处理工具", description = "用于文件处理")
@RestController
public class FileController {
    @Autowired
    private FileService fileService;

    @ApiOperation(value = "welcome")
    @GetMapping("/")
    public String upload() {
        return "welcome to libreoffice convert";
    }

    @ApiOperation(value = "libreoffice文件格式转化")
    @PostMapping("/convert")
    public ResponseEntity<byte[]> convert(@ApiParam("待转化文件") @RequestParam(value = "file") MultipartFile file,
                                          @ApiParam("目标文件格式")@RequestParam(value = "target") String target) throws IOException {
        return fileService.convertV2(file, target); //
    }

    /**
     * 上传测试代码
     * 上传文件后服务端将文件读取到内存中，而后返回该文件用于下载，无须存储到磁盘
     * @param file
     * @param target
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "上传文件，返回文件下载测试")
    @PostMapping("/download")
    public ResponseEntity<byte[]> download(@ApiParam(value = "待上传文件") @RequestParam(value = "file") MultipartFile file,
                                           @RequestParam(value = "target") String target) throws IOException {
        if(target.equals("1")){//测试UNSUPPORTED_MEDIA_TYPE返回值为415
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return fileService.download(file);
    }

    @ApiOperation(value = "获取doc文件内容")
    @PostMapping("/doc/content")
    public String getDocContent(@ApiParam(value = "doc文件")@RequestParam(value = "file") MultipartFile file) throws IOException {
        return fileService.getDocContent(file);
    }

    @ApiOperation(value = "读取docx文件内容")
    @PostMapping("/docx/content")
    public String getDocxContent(@ApiParam(value = "docx文件")@RequestParam(value = "file") MultipartFile file) throws IOException {
        return fileService.getDocxContent(file);
    }

    @ApiOperation(value = "pdf文件内容读取")
    @PostMapping("/pdf/content")
    public String getPdfContent(@ApiParam(value = "pdf文件")@RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
        return fileService.getPdfContent(multipartFile);
    }

    @ApiOperation(value = "txt文件内容读取，自侦测文件编码类型")
    @PostMapping("/txt/content")
    public String getTxtContent(@ApiParam(value = "txt文件")@RequestParam(value = "file")MultipartFile multipartFile) throws IOException {
        return fileService.getTxtContent(multipartFile);
    }

}
