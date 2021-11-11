
# Java Swing实现微信桌面端增强版。
 在微信原有功能基础上做功能增强，例如聊天记录统计分析、防撤回、自动回复、定时消息等。
# 扩展功能


## 1、防撤回功能（语音、文字、图片、视频、...）
 >![image](https://user-images.githubusercontent.com/67832925/125043492-90f7ab80-e0cd-11eb-861b-9852e3d9483b.png)![image](https://user-images.githubusercontent.com/67832925/125046970-1597f900-e0d1-11eb-9d57-b51bd689133a.png)
![image](https://user-images.githubusercontent.com/67832925/141230214-0101e829-97ac-4d6e-93f3-d0b841b56a90.png)


## 2、自动回复功能（智能聊天、酒店查询、小游戏、讲笑话、...）
 >![image](https://user-images.githubusercontent.com/67832925/125043653-bc7a9600-e0cd-11eb-8ab5-6bc64bc13c15.png)
## 3、用户属性监测（头像、昵称、签名、朋友圈动态、对方打开与你的聊天框、...）
   想象一下你的女神改个签名、打开你的聊天框你马上就能知道，那岂不是手到擒来。
 > ![image](https://user-images.githubusercontent.com/67832925/125041412-51c85b00-e0cb-11eb-9251-fc038bab3c2e.png)![image](https://user-images.githubusercontent.com/67832925/125045236-58f16800-e0cf-11eb-9de8-a8d261464e38.png)


## 4、消息轰炸(不能太频繁，否则会被禁言)
>![image](https://user-images.githubusercontent.com/67832925/125042196-272ad200-e0cc-11eb-93a7-d112e89a599b.png)
## 5、群成员统计功能(性别、城市、活跃度、...)
![image](https://user-images.githubusercontent.com/67832925/125046255-6ce99980-e0d0-11eb-938e-256e70d458c3.png)![image](https://user-images.githubusercontent.com/67832925/125046015-2eec7580-e0d0-11eb-849d-e5350bf79504.png)![image](https://user-images.githubusercontent.com/67832925/125046044-390e7400-e0d0-11eb-971d-bec954ea1424.png)![image](https://user-images.githubusercontent.com/67832925/125046064-3e6bbe80-e0d0-11eb-8860-9a9e8c3a3630.png)
## 6、共同好友的聊天记录截获
## 7、定时消息、消息定时撤回

# 安装说明(普通人员)
## 1、下载JAVA JDK安装
## 2、下载右侧的release版本，解压并双击运行wechat.vbs即可
# 安装说明(开发人员)
## 1、下载源代码，搭建开发环境
## 2、安装jintellitype-1.3.9.jar依赖到本地仓库
> 需要将resource/lib下面的二个JAR包安装到本地maven仓库，因为这个JAR经过特殊处理，中央仓库的JAR包文件不全会导致运行失败


> mvn install:install-file -Dfile="你的JAR包位置" -DgroupId=com.melloware -DartifactId=jintellitype -Dversion=1.3.9 -Dpackaging=jar
## 3、安装android.ninepatch.jar依赖到本地仓库
> mvn install:install-file -Dfile="你的JAR包位置" -DgroupId=com.android -DartifactId=ninepatch -Dversion=1.0 -Dpackaging=jar

## 4、安装jna-4.4.0.jar依赖到本地仓库
> mvn install:install-file -Dfile=wechat\src\main\resources\lib\jna-platform-4.4.0.jar -DgroupId=com.sun -DartifactId=jna -Dversion=4.4.0 -Dpackaging=jar

## 5、安装jna-platform-4.4.0.jar依赖到本地仓库
> mvn install:install-file -Dfile=wechat\src\main\resources\lib\jna-4.4.0.jar -DgroupId=com.sun -DartifactId=jna.platform -Dversion=4.4.0 -Dpackaging=jar

 # 效果预览
 
## 一、登录

 ![image](https://user-images.githubusercontent.com/67832925/127840593-ad8c9bd5-3499-4fde-bf67-46c0a349b125.png)
## 首页效果如下：
 
 ![image](https://user-images.githubusercontent.com/67832925/127836900-2dc3dfd2-8e97-40e4-961c-e060a9b22a08.png)
## 部分扩展功能
 ![image](https://user-images.githubusercontent.com/67832925/138419834-cbc525a5-fe1d-4747-ad47-891cf74891cf.png)

 
## 一、通讯录
 ![image](https://user-images.githubusercontent.com/67832925/129480174-326b368a-47c3-4d74-8b5f-a8a390264c79.png)

 ### 1、搜索、用户详细信息
![image](https://user-images.githubusercontent.com/67832925/129480655-7c1ec9bb-af3b-47c5-bd33-ea1852d6cbe0.png)

## 五、聊天
### 1、分享、链接消息、动态表情、图片
![image](https://user-images.githubusercontent.com/67832925/129480399-35d05f06-8ea6-4936-bcad-efd66f046d92.png)

### 2、上传附件

![image](https://user-images.githubusercontent.com/67832925/127843050-fe5ed785-33e0-437a-b9c0-89b631322e1f.png)

### 3、语音消息

![image](https://user-images.githubusercontent.com/67832925/129480718-0e95f58f-36f9-4b77-9484-335eec759293.png)

### 4、视频消息
![image](https://user-images.githubusercontent.com/67832925/128544101-2de42c8a-b1be-4fad-a78b-3e936d5ccbe3.png)

  
### 5、编辑消息，支持粘贴文字、文件及图片

  ![image](https://user-images.githubusercontent.com/67832925/127838840-1e12153a-908d-48f5-a498-d9371a9a6a84.png)
  
### 6、截图

  ![image](https://user-images.githubusercontent.com/67832925/127839092-1349b3b4-a7b3-4c06-b086-d3720cd1db47.png)
  

### 7、查看图片

  ![image](https://user-images.githubusercontent.com/67832925/127839251-5a4049ff-4782-4fa9-8016-b124bc8f30d0.png)


### 8、查看群成员

  ![image](https://user-images.githubusercontent.com/67832925/127839361-a30ea916-9d34-476b-abeb-37459c3dba58.png)
  
### 9、用户信息  
![image](https://user-images.githubusercontent.com/67832925/129480735-2a3828a3-1f9c-4689-9180-24e4b54b9818.png)

  
## 四、设置
### 1、个人信息

  ![image](https://user-images.githubusercontent.com/67832925/127841304-1e7d6749-837f-4ebc-85dd-ee0549bf0779.png)
  
### 2、更改头像


  ![image](https://user-images.githubusercontent.com/67832925/127842960-0d2cde9e-c6d3-43da-a4ca-e240aa5ed043.png)

  
### 3、修改密码

![image](https://user-images.githubusercontent.com/67832925/127840748-4440df63-0287-4b33-aeaa-fbf02a755437.png)
  
  
### 4、清除缓存

  ![image](https://user-images.githubusercontent.com/67832925/127837943-c4a43533-a613-452f-aa9c-8a57c33209bb.png)


### 5、关于

  ![image](https://user-images.githubusercontent.com/67832925/127837960-35d98651-6f78-4f67-8d06-f35d741e3a88.png)
  
  




 

  





> 以上几个功能只是起到抛砖引玉的作用，请打开你的脑洞吧，朋友。
借鉴了开源项目wechat_desktop、itchat4j
