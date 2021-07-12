/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : wechat

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2021-07-13 06:41:35
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for status
-- ----------------------------
DROP TABLE IF EXISTS `status`;
CREATE TABLE `status` (
  `name` varchar(200) NOT NULL,
  `undo_status` smallint(1) DEFAULT NULL COMMENT '防撤回状态1防撤回2关闭防撤回',
  `auto_status` smallint(1) DEFAULT NULL COMMENT '自动回复状态1自动回复2不自动回复',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
