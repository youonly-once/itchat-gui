/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : wechat

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2021-07-13 06:40:56
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for attr_history
-- ----------------------------
DROP TABLE IF EXISTS `attr_history`;
CREATE TABLE `attr_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `UserName` varchar(255) DEFAULT NULL COMMENT '用户名',
  `NickName` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '昵称',
  `RemarkName` varchar(255) CHARACTER SET utf8 DEFAULT '' COMMENT '备注名',
  `CreateTime` datetime DEFAULT NULL,
  `Attr` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '属性',
  `OldVal` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `NewVal` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4592 DEFAULT CHARSET=utf8mb4;
