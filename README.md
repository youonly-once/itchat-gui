# 微信桌面端增强版 (WeChat Desktop Enhanced Version)

本项目是基于微信 API + Java Swing 构建的桌面端客户端，除了保留微信的基本功能外，还增加了多项增强功能，如聊天记录统计分析、好友属性监测、防撤回、自动回复、定时消息等，极大提升了用户体验。

## QQ 群讨论：682739021

---

## 使用方法

### 1. 下载并安装 [JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

### 2. 下载并解压 [wechat.zip](https://github.com/youonly-once/itchat-gui/releases/tag/2.0) 文件

下载后解压文件至您选择的目录。

![解压界面](https://github.com/user-attachments/assets/ba944d8b-e86a-442b-8154-633d132219a9)

### 3. 配置聊天记录及聊天文件存放位置

打开 `wechat\conf\application-dev.yml` 文件，配置聊天记录和文件存放目录。

![配置文件](https://github.com/user-attachments/assets/e94847a0-4160-45d7-9408-9f69844e8e41)

### 4. 复制数据库文件

将 `wechat\conf\weixin.db` 文件复制到配置的目录（示例路径：`G:/weixin`）。

![数据库文件复制](https://github.com/user-attachments/assets/a53dc001-608b-47dc-8b34-2a99b4b25513)

### 5. 启动程序

双击 `wechat\conf\wechat.vbs` 或 `wechat\conf\wechat.bat` 启动应用。

![启动界面](https://github.com/user-attachments/assets/d803bec1-d41f-403a-ae23-a98607677c1e)


### 6. 登录成功

![首页界面](https://github.com/user-attachments/assets/731f241b-503b-4a62-b4a0-dd365c656742)

---

## 一、功能扩展介绍(旧版本界面)

### 1. 防撤回功能
- 支持语音、文字、图片、视频等消息防撤回。
- 让你看见撤回的消息背后的“秘密”！

![防撤回功能](https://github.com/youonly-once/itchat-gui/assets/67832925/9f0b3c3d-aa16-4ca4-9d64-8f3a7f4305ef)

### 2. 自动回复功能
- 提供智能聊天、酒店查询、小游戏、讲笑话等自动回复功能。
- 不再担心因没及时回复女友消息而吵架分手。

![自动回复功能](https://github.com/youonly-once/itchat-gui/assets/67832925/39ad8a0d-ebca-4746-adb3-7f11c018ecd4)

### 3. 用户属性监测
- 监控好友头像、昵称、签名、朋友圈动态等变化。
- 即时接收好友更新动态通知，让你保持亲密联系。

![用户属性监测](https://user-images.githubusercontent.com/67832925/125041412-51c85b00-e0cb-11eb-9251-fc038bab3c2e.png)
![朋友圈动态监测](https://user-images.githubusercontent.com/67832925/125045236-58f16800-e0cf-11eb-9de8-a8d261464e38.png)

### 4. 定时消息
- 通过设置频率来进行消息轰炸，需注意避免被禁言。

![消息轰炸](https://github.com/youonly-once/itchat-gui/assets/67832925/4bb9dbe0-7081-4224-b73e-9b06f558ae7c)

### 5. 好友及群成员分析
- 统计群成员的性别、城市、活跃度等信息，分析群体特征。

![群成员统计](https://github.com/youonly-once/itchat-gui/assets/67832925/31b7d2c8-f16e-4fe3-9c3b-a84cc7055a84)

---
## 常见问题

- **如何防止程序退出？**
  - 如果程序在某些操作后自动退出，确保你没有不小心修改了重要配置文件。
  
- **如何更新数据库？**
  - 确保数据库路径正确并已更新至最新版本。

---

## 类似项目

- **itchat4j**: 这是一个基于 Java 的微信个人号 API，也是本项目的灵感来源。

---

## 问题与建议

本项目会持续更新与维护，欢迎提出任何问题或建议。你可以通过以下方式联系我们：

- 在项目的 GitHub **Issues** 区域提出问题。
- 加入QQ群进行讨论：**682739021**。

---

### 贡献与支持

如果你觉得本项目对你有帮助，欢迎给我们一个 Star！如果你有想法或者改进方案，欢迎提交 Pull Request！
