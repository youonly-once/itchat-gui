/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : wechat

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2021-07-26 22:23:19
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` char(32) NOT NULL,
  `msg_id` varchar(255) DEFAULT NULL,
  `msg_type` int(255) NOT NULL,
  `app_msg_type` int(255) DEFAULT NULL,
  `msg_desc` varchar(255) NOT NULL,
  `create_time` datetime NOT NULL,
  `plaintext` mediumtext,
  `content` mediumtext COMMENT '消息内容',
  `file_path` varchar(255) DEFAULT '' COMMENT '资源文件保存路径',
  `msg_json` mediumtext NOT NULL,
  `from_username` varchar(255) DEFAULT NULL,
  `from_remarkname` varchar(255) DEFAULT NULL,
  `from_nickname` varchar(255) DEFAULT NULL,
  `from_member_of_group_username` varchar(255) DEFAULT NULL,
  `from_member_of_group_nickname` varchar(255) DEFAULT NULL,
  `from_member_of_group_displayname` varchar(255) DEFAULT NULL,
  `to_username` varchar(255) DEFAULT NULL,
  `to_remarkname` varchar(255) DEFAULT NULL,
  `to_nickname` varchar(255) DEFAULT NULL,
  `is_send` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是本人发送的消息1是0不是',
  `slave_path` varchar(255) DEFAULT NULL COMMENT '资源文件保存路径',
  `response` varchar(500) DEFAULT NULL COMMENT '消息发送结果',
  PRIMARY KEY (`id`),
  KEY `msg_id` (`msg_id`(191)) USING BTREE,
  KEY `fromname` (`from_username`(191)) USING BTREE,
  KEY `toname` (`to_username`(191)) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
