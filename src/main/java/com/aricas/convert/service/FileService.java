package com.aricas.convert.service;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

@Service
public class FileService {

    public ResponseEntity<byte[]> convert(MultipartFile multipartFile, String targetFormat) throws IOException {

        //输入文件
        System.out.println(targetFormat);
        String originalFilename = multipartFile.getOriginalFilename();
        String[] filename = originalFilename.split("\\.");
        File file = File.createTempFile(filename[0], "."+filename[1]);
        multipartFile.transferTo(file);


        //输出文件
        String outPutFileName = filename[0];
        if(outPutFileName.length()<3){
            outPutFileName += "output";
        }
        File outPutFile = File.createTempFile(outPutFileName, "."+targetFormat);

        //开始转化
        LocalOfficeManager officeManager = LocalOfficeManager.install();
        try {
            officeManager.start();
            JodConverter.convert(file)
                    .to(outPutFile)
                    .execute(); //执行文件转化
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", originalFilename.replace(filename[1], targetFormat));

            //File对象转化为byte
            byte[] data = null;
            FileInputStream fileInputStream = new FileInputStream(outPutFile);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fileInputStream.read(buffer)) != -1){
                byteOutputStream.write(buffer, 0, len);
            }
            data = byteOutputStream.toByteArray();
            fileInputStream.close();
            byteOutputStream.close();
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (OfficeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }finally {
            OfficeUtils.stopQuietly(officeManager);
            file.deleteOnExit();//文件退出后删除该文件
        }
    }
    public void test(){
        File inputFile = new File("/root/office/input.doc");
        File outputFile = new File("/root/office/document.pdf");

        LocalOfficeManager officeManager = LocalOfficeManager.install();
        try {
            officeManager.start();
            JodConverter.convert(inputFile)
                    .to(outputFile)
                    .execute();
        } catch (OfficeException e) {
            e.printStackTrace();
        }finally {
            OfficeUtils.stopQuietly(officeManager);
        }
    }

    public ResponseEntity<byte[]> download(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String []names = originalFilename.split("\\.");
        File getFile = File.createTempFile(names[0]+"2020", names[1]);
        file.transferTo(getFile);

        FileInputStream fileInputStream = new FileInputStream(getFile);
        ByteOutputStream byteOutputStream = new ByteOutputStream();

        int len = 0;
        byte[] data = null;
        byte[] buffer = new byte[1024];
        while (((len = fileInputStream.read(buffer))!=-1)) {
            byteOutputStream.write(buffer, 0, len);
        }
        data = byteOutputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(getFile.getName(),"utf-8"));
        return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
    }
}
