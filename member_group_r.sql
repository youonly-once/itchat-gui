/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : wechat

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2021-07-13 06:41:20
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for member_group_r
-- ----------------------------
DROP TABLE IF EXISTS `member_group_r`;
CREATE TABLE `member_group_r` (
  `GroupUserName` varchar(255) NOT NULL,
  `MemberUserName` varchar(255) NOT NULL,
  `id` char(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
