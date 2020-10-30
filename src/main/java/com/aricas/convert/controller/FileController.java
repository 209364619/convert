package com.aricas.convert.controller;

import com.aricas.convert.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileController {
    @Autowired
    private FileService fileService;

    @GetMapping("/")
    public String upload() {
        return "welcome to libreoffice convert";
    }

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convert(@RequestParam(value = "file") MultipartFile file,
                                          @RequestParam(value = "target") String target) throws IOException {
        return fileService.convert(file, target);
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam(value = "file") MultipartFile file,
                                           @RequestParam(value = "target") String target) throws IOException {
        if(target.equals("1")){
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return fileService.download(file);
    }
}
