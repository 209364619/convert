# libreoffice对word文件进行转换
## 服务状态测试
GET:http://url:8080/
reponse: welcome to libreoffice convert
## 上传并获取转化后的文件
POST:http://url:8080/convert
Content-Type:multipart/form-data
 - parma1: file   -->bin
 - parma2: target -->String
>将待转化文件上传到服务端，若转化完成，则返回转化后的文件,转化失败返回值415。目前测试可转化文件格式有：
+ doc <--> docx
+ doc --> txt
+ docx -- txt
+ doc --> pdf
+ docx -- pdf
# 该服务以结合打包到docker容器内，可通过拉取镜像查看效果
docker pull registry.cn-hangzhou.aliyuncs.com/hph/libreoffice:v2
docker run -itd -p 8080:8080 registry.cn-hangzhou.aliyuncs.com/hph/libreoffice:v2
