package com.aricas.convert.service;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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

import java.io.*;
import java.net.URLEncoder;
import java.util.UUID;

@Service
public class FileService {

    public ResponseEntity<byte[]> convert(MultipartFile multipartFile, String targetFormat) throws IOException {

        //输入文件
        String originalFilename = multipartFile.getOriginalFilename();
        String[] filename = originalFilename.split("\\.");
        File file = File.createTempFile(filename[0], "." + filename[1]);
        multipartFile.transferTo(file);


        //输出文件
        String outPutFileName = filename[0];
        if (outPutFileName.length() < 3) { // createTempFile要求文件名长度必须>=3,否则抛出异常
            outPutFileName += "output";
        }
        File outPutFile = File.createTempFile(outPutFileName, "." + targetFormat);

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
            while ((len = fileInputStream.read(buffer)) != -1) {
                byteOutputStream.write(buffer, 0, len);
            }
            data = byteOutputStream.toByteArray();
            fileInputStream.close();
            byteOutputStream.close();
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (OfficeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        } finally {
            OfficeUtils.stopQuietly(officeManager);
            file.deleteOnExit();//文件退出后删除该文件
        }
    }

    /**
     * 1.创建File(临时文件)
     * 2.multipartfile文件转化为File文件
     * 3.将原始文件转化为特定格式文件
     * 4.使用FileUtils将File转化为byte[]返回
     * 上传文件大小不可大于2GB，文件转化失败或者格式不支持则返回状态码：UNSUPPORTED_MEDIA_TYPE(415)
     *
     * @param multipartFile 上传文件
     * @param targetFormat  目标格式
     * @return
     * @throws IOException
     */
    public ResponseEntity<byte[]> convertV2(MultipartFile multipartFile, String targetFormat) throws IOException {

        //输入文件
        System.out.println(targetFormat);
        String originalFilename = multipartFile.getOriginalFilename();
        String[] filename = originalFilename.split("\\.");
        File file = File.createTempFile(filename[0], "." + filename[1]);
        multipartFile.transferTo(file);


        //输出文件
        String outPutFileName = filename[0];
        if (outPutFileName.length() < 3) { // createTempFile要求文件名长度必须>=3,否则抛出异常
            outPutFileName += "output";
        }
        File outPutFile = File.createTempFile(outPutFileName, "." + targetFormat);

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

            data = FileUtils.readFileToByteArray(outPutFile); //该方法省完成了FileInputStream->byteOutputStream->byte[]的繁琐转化
            //上传文件大小必须小于2GB否则抛出异常
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (OfficeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        } finally {
            OfficeUtils.stopQuietly(officeManager);
            file.deleteOnExit();//文件退出后删除该文件
        }
    }

    public ResponseEntity<byte[]> download(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String[] names = originalFilename.split("\\.");
        File getFile = File.createTempFile(names[0] + "2020", names[1]);
        file.transferTo(getFile);

        FileInputStream fileInputStream = new FileInputStream(getFile);
        ByteOutputStream byteOutputStream = new ByteOutputStream();

        int len = 0;
        byte[] data = null;
        byte[] buffer = new byte[1024];
        while (((len = fileInputStream.read(buffer)) != -1)) {
            byteOutputStream.write(buffer, 0, len);
        }
        data = byteOutputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(getFile.getName(), "utf-8"));
        return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
    }

    /**
     * 读取上传doc文件内容
     *
     * @param multipartFile
     * @return
     */
    public String getDocContent(MultipartFile multipartFile) throws IOException {
        FileInputStream fileInputStream = null;
        HWPFDocument doc = null;
        try {
            //MutipartFile-->File-->FileInputStream
            File file = File.createTempFile(UUID.randomUUID().toString(), ".doc");
            multipartFile.transferTo(file);
            fileInputStream = new FileInputStream(file);
            doc = new HWPFDocument(fileInputStream);
            String result = doc.getDocumentText();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileInputStream.close();
            doc.close();
        }
        return "doc文件解析异常，未能读取文件内容";

    }

    /**
     * 读取上传docx文件内容
     *
     * @param multipartFile
     * @return
     */
    public String getDocxContent(MultipartFile multipartFile) throws IOException {
        File file = null;
        FileInputStream fileInputStream = null;
        XWPFDocument xwpfDocument = null;
        try {
            file = File.createTempFile(UUID.randomUUID().toString(), ".docx");
            multipartFile.transferTo(file);
            fileInputStream = new FileInputStream(file);

            xwpfDocument = new XWPFDocument(fileInputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(xwpfDocument);
            return extractor.getText();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileInputStream.close();
            xwpfDocument.close();
        }
        return "docx读取异常，未能提取文件内容！";
    }

    public String getPdfContent(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".pdf");
        multipartFile.transferTo(file);

        try {
            PDDocument document = PDDocument.load(file);
            document.getClass();

            if (!document.isEncrypted()) { //文件未加密，读取内容
//                PDFTextStripperByArea pdfTextStripperByArea = new PDFTextStripperByArea();
//                pdfTextStripperByArea.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();
                String text = tStripper.getText(document);
                return text;

            }
            return "文件加密，无法读取内容";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "pdf解析异常";
    }

    /**
     * 获取txt文本编码
     *
     * @param file
     * @return
     * @throws IOException
     */
    public String getCharset(File file) throws IOException {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                return charset; //文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF
                    && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; //文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; //文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; //文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
     * 根据txt文本编码读取文件内容
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public String getTxtContent(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt");
        multipartFile.transferTo(file); //multipartfile转化为File文件
        String fileEncoding = getCharset(file);//获取文本的编码

        InputStreamReader inputStreamReader =
                new InputStreamReader(new FileInputStream(file), fileEncoding);//根据文件编码读取文件内容
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder result = new StringBuilder();
        String temp = null;
        while ((temp = bufferedReader.readLine()) != null) {
            result.append(temp);
        }
        return result.toString();
    }
}
