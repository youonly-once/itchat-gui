/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : wechat

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2021-07-13 06:41:09
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for contacts
-- ----------------------------
DROP TABLE IF EXISTS `contacts`;
CREATE TABLE `contacts` (
  `ChatRoomId` double DEFAULT NULL,
  `Sex` double DEFAULT NULL,
  `AttrStatus` double DEFAULT NULL,
  `Statues` double DEFAULT NULL,
  `PYQuanPin` varchar(500) DEFAULT NULL,
  `EncryChatRoomId` varchar(500) DEFAULT NULL,
  `DisplayName` varchar(500) DEFAULT NULL,
  `VerifyFlag` double DEFAULT NULL,
  `UniFriend` double DEFAULT NULL,
  `ContactFlag` double DEFAULT NULL,
  `UserName` varchar(100) NOT NULL,
  `MemberList` varchar(500) DEFAULT NULL,
  `StarFriend` double DEFAULT NULL,
  `HeadImgUrl` varchar(500) DEFAULT NULL,
  `AppAccountFlag` double DEFAULT NULL,
  `MemberCount` double DEFAULT NULL,
  `RemarkPYInitial` varchar(500) DEFAULT NULL,
  `City` varchar(500) DEFAULT NULL,
  `NickName` varchar(500) DEFAULT NULL,
  `Province` varchar(500) DEFAULT NULL,
  `SnsFlag` double DEFAULT NULL,
  `Alias` varchar(500) DEFAULT NULL,
  `KeyWord` varchar(500) DEFAULT NULL,
  `HideInputBarFlag` double DEFAULT NULL,
  `Signature` varchar(500) DEFAULT NULL,
  `RemarkName` varchar(500) DEFAULT NULL,
  `RemarkPYQuanPin` varchar(500) DEFAULT NULL,
  `Uin` double DEFAULT NULL,
  `OwnerUin` double DEFAULT NULL,
  `IsOwner` double DEFAULT NULL,
  `PYInitial` varchar(500) DEFAULT NULL,
  `IsContacts` tinyint(1) DEFAULT '0' COMMENT '是否为联系人(false则为群成员)',
  PRIMARY KEY (`UserName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
