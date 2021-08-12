/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : wechat

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2021-08-12 22:40:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for attr_history
-- ----------------------------
DROP TABLE IF EXISTS `attr_history`;
CREATE TABLE `attr_history` (
`id`  int(11) NOT NULL AUTO_INCREMENT ,
`UserName`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名' ,
`NickName`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称' ,
`RemarkName`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '备注名' ,
`CreateTime`  datetime NULL DEFAULT NULL ,
`Attr`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '属性' ,
`OldVal`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
`NewVal`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci
AUTO_INCREMENT=4803

;

-- ----------------------------
-- Table structure for contacts
-- ----------------------------
DROP TABLE IF EXISTS `contacts`;
CREATE TABLE `contacts` (
`ChatRoomId`  double NULL DEFAULT NULL ,
`Sex`  double NULL DEFAULT NULL ,
`AttrStatus`  double NULL DEFAULT NULL ,
`Statues`  double NULL DEFAULT NULL ,
`PYQuanPin`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`EncryChatRoomId`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`DisplayName`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`VerifyFlag`  double NULL DEFAULT NULL ,
`UniFriend`  double NULL DEFAULT NULL ,
`ContactFlag`  double NULL DEFAULT NULL ,
`UserName`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`MemberList`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`StarFriend`  double NULL DEFAULT NULL ,
`HeadImgUrl`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`AppAccountFlag`  double NULL DEFAULT NULL ,
`MemberCount`  double NULL DEFAULT NULL ,
`RemarkPYInitial`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`City`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`NickName`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`Province`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`SnsFlag`  double NULL DEFAULT NULL ,
`Alias`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`KeyWord`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`HideInputBarFlag`  double NULL DEFAULT NULL ,
`Signature`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`RemarkName`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`RemarkPYQuanPin`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`Uin`  double NULL DEFAULT NULL ,
`OwnerUin`  double NULL DEFAULT NULL ,
`IsOwner`  double NULL DEFAULT NULL ,
`PYInitial`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`IsContacts`  tinyint(1) NULL DEFAULT 0 COMMENT '是否为联系人(false则为群成员)' ,
PRIMARY KEY (`UserName`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci

;

-- ----------------------------
-- Table structure for member_group_r
-- ----------------------------
DROP TABLE IF EXISTS `member_group_r`;
CREATE TABLE `member_group_r` (
`GroupUserName`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`MemberUserName`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`id`  char(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci

;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
`id`  char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`msg_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`msg_type`  int(255) NOT NULL ,
`app_msg_type`  int(255) NULL DEFAULT NULL ,
`msg_desc`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`create_time`  datetime NOT NULL ,
`plaintext`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL ,
`content`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容' ,
`file_path`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '资源文件保存路径' ,
`msg_json`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`from_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_displayname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`is_send`  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是本人发送的消息1是0不是' ,
`slave_path`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '缩略图路径' ,
`response`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息发送结果' ,
`play_length`  bigint(20) NULL DEFAULT 0 COMMENT '视频长度' ,
`img_height`  int(11) NULL DEFAULT 0 COMMENT '缩略图高度' ,
`img_width`  int(11) NULL DEFAULT 0 COMMENT '缩略图宽度' ,
`voice_length`  bigint(20) NULL DEFAULT 0 COMMENT '语音长度' ,
PRIMARY KEY (`id`),
INDEX `msg_id` (`msg_id`(191)) USING BTREE ,
INDEX `create_time` (`create_time`) USING BTREE ,
INDEX `union_index` (`from_username`(191), `to_username`(191)) USING BTREE 
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci

;

-- ----------------------------
-- Table structure for message_20210720
-- ----------------------------
DROP TABLE IF EXISTS `message_20210720`;
CREATE TABLE `message_20210720` (
`id`  char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`msg_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`msg_type`  int(255) NOT NULL ,
`app_msg_type`  int(255) NULL DEFAULT NULL ,
`msg_desc`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`create_time`  datetime NOT NULL ,
`content`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容' ,
`file_path`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '资源文件保存路径' ,
`msg_json`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`from_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_displayname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`is_send`  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是本人发送的消息1是0不是' ,
`slave_path`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源文件保存路径' ,
PRIMARY KEY (`id`),
INDEX `msg_id` (`msg_id`(191)) USING BTREE ,
INDEX `fromname` (`from_username`(191)) USING BTREE ,
INDEX `toname` (`to_username`(191)) USING BTREE 
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci

;

-- ----------------------------
-- Table structure for message1
-- ----------------------------
DROP TABLE IF EXISTS `message1`;
CREATE TABLE `message1` (
`id`  char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`msg_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`msg_type`  int(255) NOT NULL ,
`app_msg_type`  int(255) NULL DEFAULT NULL ,
`msg_desc`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`create_time`  datetime NOT NULL ,
`content`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容' ,
`file_path`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '资源文件保存路径' ,
`msg_json`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`from_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_displayname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`is_send`  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是本人发送的消息1是0不是' 
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci

;

-- ----------------------------
-- Table structure for message2
-- ----------------------------
DROP TABLE IF EXISTS `message2`;
CREATE TABLE `message2` (
`id`  char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`msg_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`msg_type`  int(255) NOT NULL ,
`app_msg_type`  int(255) NULL DEFAULT NULL ,
`msg_desc`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`create_time`  datetime NOT NULL ,
`content`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容' ,
`file_path`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '资源文件保存路径' ,
`msg_json`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`from_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`from_member_of_group_displayname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_username`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_remarkname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`to_nickname`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL ,
`is_send`  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是本人发送的消息1是0不是' 
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci

;

-- ----------------------------
-- Table structure for status
-- ----------------------------
DROP TABLE IF EXISTS `status`;
CREATE TABLE `status` (
`name`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`undo_status`  smallint(1) NULL DEFAULT NULL COMMENT '防撤回状态1防撤回2关闭防撤回' ,
`auto_status`  smallint(1) NULL DEFAULT NULL COMMENT '自动回复状态1自动回复2不自动回复' ,
PRIMARY KEY (`name`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci

;

-- ----------------------------
-- Auto increment value for attr_history
-- ----------------------------
ALTER TABLE `attr_history` AUTO_INCREMENT=4803;
