# WeChat Desktop Enhanced Version

This project is a desktop client built using the WeChat API and Java Swing. In addition to retaining the basic WeChat functionality, it introduces several enhanced features such as chat history statistics, friend attribute monitoring, anti-recall, auto-reply, scheduled messages, and more, greatly improving the user experience.

## QQ Group Discussion: 682739021

---

## How to Use

### 1. Download and Install [JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

### 2. Download and Extract the [wechat.zip](https://github.com/youonly-once/itchat-gui/releases/tag/2.0) File

After downloading, extract the file to your preferred directory.

![Extract Interface](https://github.com/user-attachments/assets/ba944d8b-e86a-442b-8154-633d132219a9)

### 3. Configure Chat History and File Storage Paths

Open the `wechat\conf\application-dev.yml` file and configure the paths for chat history and file storage.

![Configuration File](https://github.com/user-attachments/assets/e94847a0-4160-45d7-9408-9f69844e8e41)

### 4. Copy the Database File

Copy the `wechat\conf\weixin.db` file to the configured directory (Example path: `G:/weixin`).

![Database File Copy](https://github.com/user-attachments/assets/a53dc001-608b-47dc-8b34-2a99b4b25513)

### 5. Launch the Application

Double-click the `wechat\conf\wechat.vbs` or `wechat\conf\wechat.bat` file to start the application.

![Startup Interface](https://github.com/user-attachments/assets/d803bec1-d41f-403a-ae23-a98607677c1e)

### 6. Successful Login

![Homepage Interface](https://github.com/user-attachments/assets/731f241b-503b-4a62-b4a0-dd365c656742)

---

## I. Feature Enhancements (Old Version Interface)

### 1. Anti-Recall Feature
- Supports anti-recall for voice, text, images, video messages, etc.
- You can see the “secrets” behind the recalled messages!

![Anti-Recall Feature](https://github.com/youonly-once/itchat-gui/assets/67832925/9f0b3c3d-aa16-4ca4-9d64-8f3a7f4305ef)

### 2. Auto-Reply Feature
- Provides intelligent chat, hotel queries, mini-games, joke delivery, and more.
- Never worry about missing a reply to your girlfriend and causing a quarrel.

![Auto-Reply Feature](https://github.com/youonly-once/itchat-gui/assets/67832925/39ad8a0d-ebca-4746-adb3-7f11c018ecd4)

### 3. User Attribute Monitoring
- Monitors changes in friend’s avatar, nickname, signature, and Moments updates.
- Receive real-time notifications of friends' updates to stay closely connected.

![User Attribute Monitoring](https://user-images.githubusercontent.com/67832925/125041412-51c85b00-e0cb-11eb-9251-fc038bab3c2e.png)
![Moments Monitoring](https://user-images.githubusercontent.com/67832925/125045236-58f16800-e0cf-11eb-9de8-a8d261464e38.png)

### 4. Scheduled Messages
- Set a frequency for message bombing, but be cautious to avoid being muted.

![Message Bombing](https://github.com/youonly-once/itchat-gui/assets/67832925/4bb9dbe0-7081-4224-b73e-9b06f558ae7c)

### 5. Friend and Group Member Analysis
- Analyze group members' gender, city, activity levels, and more, to gain insights into group characteristics.

![Group Member Statistics](https://github.com/youonly-once/itchat-gui/assets/67832925/31b7d2c8-f16e-4fe3-9c3b-a84cc7055a84)

---

## Frequently Asked Questions

- **How to prevent the program from exiting?**
  - If the program exits automatically after certain actions, ensure that no important configuration files have been accidentally modified.
  
- **How to update the database?**
  - Ensure that the database path is correctly set and updated to the latest version.

---

## Similar Projects

- **itchat4j**: An excellent Java-based WeChat personal API, which also inspired this project.

---

## Issues and Suggestions

This project will continue to be updated and maintained. Feel free to raise any issues or suggestions. You can contact us through the following:

- Post issues on the **GitHub Issues** section of the project.
- Join our QQ group for discussions: **682739021**.

---

### Contribution and Support

If you find this project useful, feel free to give it a Star! If you have ideas or improvements, you are welcome to submit a Pull Request!
