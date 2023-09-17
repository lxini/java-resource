/*
SQLyog Ultimate v12.5.1 (64 bit)
MySQL - 5.5.42-log : Database - edu_ad
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`edu_ad` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `edu_ad`;

/*Table structure for table `ad_show_tj` */

DROP TABLE IF EXISTS `ad_show_tj`;

CREATE TABLE `ad_show_tj` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fId` int(11) DEFAULT NULL,
  `fType` varchar(255) DEFAULT NULL,
  `showNum` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `createTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `ad_show_tj` */

/*Table structure for table `operate_log` */

DROP TABLE IF EXISTS `operate_log`;

CREATE TABLE `operate_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fId` int(11) DEFAULT NULL COMMENT '关联id',
  `fType` varchar(255) DEFAULT NULL COMMENT '日志类型',
  `operateName` varchar(255) DEFAULT NULL COMMENT '操作内容',
  `operateDetail` varchar(2048) DEFAULT NULL COMMENT '操作详情',
  `operator` varchar(255) DEFAULT NULL COMMENT '操作者',
  `createTime` datetime DEFAULT NULL,
  `isDel` int(11) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统日志';

/*Data for the table `operate_log` */

/*Table structure for table `portal` */

DROP TABLE IF EXISTS `portal`;

CREATE TABLE `portal` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '入口描述',
  `logo` varchar(256) NOT NULL COMMENT '图片url',
  `url` varchar(256) NOT NULL COMMENT '跳转url',
  `sort` int(11) NOT NULL COMMENT '排序',
  `disable` int(1) DEFAULT '0' COMMENT '是否启用，1 不可用 0 可用 默认0 ',
  `portType` varchar(255) DEFAULT NULL COMMENT '类型（WORKPLACE 职场版，SCHOOL 校招版）',
  `startTime` datetime DEFAULT NULL COMMENT '开始时间',
  `endTime` datetime DEFAULT NULL COMMENT '结束时间',
  `subsite` varchar(255) DEFAULT NULL COMMENT '分站',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` varchar(128) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `isDel` int(1) DEFAULT '0' COMMENT '是否删除，1 已删除 0 未删除 默认0 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='首页入口配置表';

/*Data for the table `portal` */

/*Table structure for table `position_dict_info` */

DROP TABLE IF EXISTS `position_dict_info`;

CREATE TABLE `position_dict_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `positionKey` varchar(256) NOT NULL COMMENT '职位关键词',
  `category1` varchar(128) NOT NULL COMMENT '职位一级分类，如：技术、运营、金融、设计等',
  `category2` varchar(128) NOT NULL COMMENT '职位二级分类，如：视觉设计、后端开发、DBA、测试',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` varchar(128) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `isDel` int(1) DEFAULT '0' COMMENT '是否删除，1 已删除 0 未删除 默认0 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='职位词典信息表';

/*Data for the table `position_dict_info` */

/*Table structure for table `promote_company` */

DROP TABLE IF EXISTS `promote_company`;

CREATE TABLE `promote_company` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `companySpaceId` int(11) NOT NULL COMMENT 'space ID',
  `companyId` int(11) NOT NULL COMMENT '公司ID',
  `companyShortName` varchar(64) DEFAULT NULL COMMENT '公司简称',
  `logo` varchar(256) NOT NULL DEFAULT '' COMMENT 'logo',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDel` int(1) DEFAULT '0' COMMENT '是否删除，1 已删除 0 未删除 默认0 ',
  `position1` int(11) DEFAULT NULL COMMENT '职位id',
  `position2` int(11) DEFAULT NULL COMMENT '职位id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='推荐公司信息表';

/*Data for the table `promote_company` */

/*Table structure for table `promote_company_space` */

DROP TABLE IF EXISTS `promote_company_space`;

CREATE TABLE `promote_company_space` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(64) NOT NULL COMMENT '推广位title',
  `ordered` int(1) DEFAULT '0' COMMENT '0为有序，1为随机',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDel` int(1) DEFAULT '0' COMMENT '是否删除，1 已删除 0 未删除 默认0 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='推荐公司信息表';

/*Data for the table `promote_company_space` */

/*Table structure for table `promote_hr` */

DROP TABLE IF EXISTS `promote_hr`;

CREATE TABLE `promote_hr` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tabId` int(11) NOT NULL COMMENT 'tab id',
  `hrId` int(11) NOT NULL COMMENT 'hr ID',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDel` int(1) DEFAULT '0' COMMENT '是否删除，1 已删除 0 未删除 默认0 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='推荐tab对应HR信息表';

/*Data for the table `promote_hr` */

/*Table structure for table `promote_hr_tab` */

DROP TABLE IF EXISTS `promote_hr_tab`;

CREATE TABLE `promote_hr_tab` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(64) NOT NULL COMMENT 'title',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDel` int(1) DEFAULT '0' COMMENT '是否删除，1 已删除 0 未删除 默认0 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='推荐HR tab信息表';

/*Data for the table `promote_hr_tab` */

/*Table structure for table `promotion_ad` */

DROP TABLE IF EXISTS `promotion_ad`;

CREATE TABLE `promotion_ad` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT '广告名',
  `spaceId` int(11) DEFAULT NULL COMMENT '广告位id',
  `keyword` varchar(255) DEFAULT NULL COMMENT '精确搜索关键词',
  `htmlContent` text COMMENT '静态广告的内容',
  `text` varchar(255) DEFAULT NULL COMMENT '文字一',
  `link` varchar(255) DEFAULT NULL COMMENT '链接一',
  `startTime` datetime DEFAULT NULL COMMENT '开始时间',
  `endTime` datetime DEFAULT NULL COMMENT '结束时间',
  `createTime` datetime DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `status` int(2) NOT NULL DEFAULT '0',
  `priority` int(4) DEFAULT '0' COMMENT '优先级',
  `img` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `promotion_ad_SEG` (`spaceId`,`startTime`,`endTime`,`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1095 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `promotion_ad` */

insert  into `promotion_ad`(`id`,`name`,`spaceId`,`keyword`,`htmlContent`,`text`,`link`,`startTime`,`endTime`,`createTime`,`updateTime`,`status`,`priority`,`img`) values 
(1074,'java基础训练营1111',1,NULL,NULL,'sdfsadf','https://edu.lagou.com/','2020-06-23 17:03:27','2020-08-30 17:03:45','2020-07-14 14:28:34','2020-09-16 15:24:55',1,0,'https://edu-lagou.oss-cn-beijing.aliyuncs.com/images/2020/07/17/15949658736951644.jpeg'),
(1075,'精选课程',2,NULL,NULL,NULL,'http://edufront.lagou.com/#/content?courseId=1','2020-06-29 17:03:25','2020-08-30 17:03:45','2020-07-14 14:28:34','2020-07-17 13:13:51',1,0,'https://edu-lagou.oss-cn-beijing.aliyuncs.com/images/2020/07/17/15949580209796992.png'),
(1076,'java训练营2',3,NULL,NULL,NULL,'http://edufront.lagou.com/#/content?courseId=1','2020-06-29 17:03:25','2020-08-30 17:03:45','2020-07-14 14:28:34','2020-07-17 13:14:11',1,0,'https://edu-lagou.oss-cn-beijing.aliyuncs.com/images/2020/07/17/15949583460826312.jpeg'),
(1077,'轮播广告2',3,NULL,NULL,NULL,'http://edufront.lagou.com/#/content?courseId=1','2020-06-29 17:03:25','2020-08-31 17:03:45','2020-07-14 14:28:34','2020-07-17 13:07:52',1,0,'https://edu-lagou.oss-cn-beijing.aliyuncs.com/images/2020/07/17/15949624525374063.png'),
(1078,'广告3333',162,NULL,NULL,'这是文本内容111','http://www.163.com111','2020-01-01 00:00:00','2020-09-30 00:00:00','2020-07-14 14:28:34','2020-07-17 11:22:31',0,2,'https://edu-lagou.oss-cn-beijing.aliyuncs.com/images/2020/07/17/15949561472241579.jpg'),
(1079,'广告',162,NULL,NULL,'这是文本内容111','http://www.163.com111','2020-07-01 00:00:00','2020-08-01 00:00:00','2020-07-14 14:30:48','2020-07-14 14:30:48',0,2,'http://www.baidu.com111'),
(1080,'广告名称111',169,NULL,NULL,'text','link1','2030-01-10 01:01:01','2030-01-02 12:12:12','2020-07-14 16:38:03','2020-07-14 17:36:34',0,0,'img1'),
(1081,'广告名称111',169,NULL,NULL,'text','link1','2030-01-10 01:01:01','2030-01-02 12:12:12','2020-07-14 17:36:50','2020-07-14 17:36:50',0,0,'img1'),
(1082,'111',NULL,NULL,NULL,NULL,NULL,'2020-07-14 16:00:00','2020-07-15 16:00:00','2020-07-15 11:57:23','2020-07-15 11:57:23',0,0,NULL),
(1083,'222',NULL,NULL,NULL,NULL,NULL,'2020-07-14 16:00:00','2020-07-16 16:00:00','2020-07-15 11:58:00','2020-07-15 11:58:00',0,0,NULL),
(1084,'123123',NULL,NULL,NULL,NULL,NULL,'2020-07-15 16:00:00','2020-07-16 16:00:00','2020-07-16 15:52:30','2020-07-16 15:52:30',1,0,NULL),
(1085,'storm',NULL,NULL,NULL,NULL,NULL,'2020-07-15 15:06:02','2020-07-30 16:00:00','2020-07-16 21:48:26','2020-07-16 21:48:26',0,0,NULL),
(1086,'stormtest',3,NULL,NULL,NULL,NULL,'2020-07-16 16:00:00','2020-07-17 16:00:00','2020-07-16 22:16:48','2020-07-16 22:16:48',0,0,NULL),
(1087,'撒短发',3,NULL,NULL,NULL,'sdfasdfasdfssss','2020-07-14 16:00:00','2020-07-16 16:00:00','2020-07-16 22:22:47','2020-07-16 22:22:47',0,0,NULL),
(1088,'冰淇淋套餐',NULL,NULL,NULL,NULL,'sdfdasdf','2020-01-01 00:00:00','2020-02-01 01:00:00','2020-07-17 10:07:39','2020-07-17 11:10:51',0,0,NULL),
(1089,'dfgfd',1,NULL,NULL,'gdfg','fdgdg','2020-07-07 16:00:00','2020-07-20 16:00:00','2020-07-30 11:13:32','2020-07-30 11:13:32',0,0,'https://edu-lagou.oss-cn-beijing.aliyuncs.com/images/2020/07/30/15960787995181062.png'),
(1091,'测试广告555',555,NULL,'bbbb','https://edu.lagou.com/','https://edu.lagou.com/','2020-01-01 10:00:00','2020-01-01 10:00:00','2020-08-07 08:05:15','2020-08-07 21:59:39',1,10,'https://edu.lagou.com/'),
(1092,'测试广告555',555,NULL,'bbbb','https://edu.lagou.com/','https://edu.lagou.com/','2020-01-01 10:00:00','2020-01-01 10:00:00','2020-08-07 21:59:51','2020-08-07 21:59:51',1,10,'https://edu.lagou.com/'),
(1093,'1sddfdasaf',2,NULL,NULL,'11','11','2020-08-03 16:00:00','2020-08-05 16:00:00','2020-08-28 21:10:16','2020-08-28 21:10:16',0,0,NULL),
(1094,'33',181,NULL,NULL,NULL,'33','2020-09-06 16:00:00','2020-09-24 16:00:00','2020-09-15 18:18:27','2020-09-15 18:24:48',0,0,NULL);

/*Table structure for table `promotion_ad_temp` */

DROP TABLE IF EXISTS `promotion_ad_temp`;

CREATE TABLE `promotion_ad_temp` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL COMMENT '广告名',
  `promotionSpaceId` int(11) DEFAULT NULL COMMENT '广告位id',
  `subsite` varchar(255) DEFAULT NULL COMMENT '分站',
  `keyword` varchar(255) DEFAULT NULL,
  `fromDepartment` varchar(255) DEFAULT NULL COMMENT '部门',
  `fromMan` varchar(255) DEFAULT NULL COMMENT '负责人',
  `companyId` int(11) DEFAULT NULL COMMENT '公司id',
  `companyShortName` varchar(255) DEFAULT NULL COMMENT '公司简称',
  `htmlContent` varchar(4000) DEFAULT NULL,
  `text1` varchar(255) DEFAULT NULL COMMENT '文字一',
  `text2` varchar(255) DEFAULT NULL COMMENT '文字二',
  `text3` varchar(255) DEFAULT NULL COMMENT '文字三',
  `img1` varchar(255) DEFAULT NULL COMMENT '图片一',
  `img1_small` varchar(255) DEFAULT NULL COMMENT '图片1小图',
  `img2` varchar(255) DEFAULT NULL COMMENT '图片二',
  `img2_small` varchar(255) DEFAULT NULL COMMENT '图片二小图',
  `img3` varchar(255) DEFAULT NULL COMMENT '图片三',
  `img3_small` varchar(255) DEFAULT NULL COMMENT '图片三小图',
  `link1` varchar(255) DEFAULT NULL COMMENT '链接一',
  `link2` varchar(255) DEFAULT NULL COMMENT '链接二',
  `width` int(11) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `backgroundColor` varchar(255) DEFAULT NULL COMMENT '背景色',
  `startTime` datetime DEFAULT NULL COMMENT '开始时间',
  `endTime` datetime DEFAULT NULL COMMENT '结束时间',
  `eventTime` datetime DEFAULT NULL COMMENT '事件时间',
  `location` int(11) DEFAULT NULL COMMENT '位置',
  `createTime` datetime DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `isDel` int(2) NOT NULL DEFAULT '0',
  `positionCategory1` varchar(255) DEFAULT NULL COMMENT '职位一级分类',
  `positionCategory2` varchar(255) DEFAULT NULL COMMENT '职位二级分类',
  `pay` int(2) DEFAULT '0' COMMENT '是否付费',
  `contract` varchar(255) DEFAULT NULL COMMENT '合同编号',
  `priority` int(4) DEFAULT '0' COMMENT '优先级',
  `adType` int(4) DEFAULT '0' COMMENT '类型,1:app端可沟通职位广告',
  `adContentType` int(4) DEFAULT '0' COMMENT '广告内容类型，０-图片,1-职位',
  `positionId` int(11) DEFAULT NULL COMMENT '职位ID',
  `summary` varchar(255) DEFAULT NULL COMMENT '职位概要,一句话',
  `subscriptUrl` varchar(255) DEFAULT NULL COMMENT '角标url',
  `subscriptId` int(11) DEFAULT NULL COMMENT '角标样式id',
  `adStyleUrl` varchar(255) DEFAULT NULL COMMENT '广告样式url',
  `adStyleId` int(11) DEFAULT NULL COMMENT '广告样式id',
  `isShowAdSign` int(2) DEFAULT NULL COMMENT '是否使用广告标识',
  `isShowLogo` int(2) DEFAULT NULL COMMENT '是否展示logo',
  `showFrequency` int(11) DEFAULT NULL COMMENT '展示频次 0展示一次 1展示多次',
  `isGraduates` int(11) DEFAULT NULL COMMENT '0:全部,1:应届生,2:社招',
  `userId` varchar(255) DEFAULT NULL COMMENT '白名单用户id，多个用逗号分隔',
  `iswhite` bit(1) DEFAULT b'0' COMMENT '是否支持白名单 0 不支持，1：支持',
  `putinversion` int(1) DEFAULT '0' COMMENT '投放版本：0全部，1企业版，2用户版',
  `filed2` varchar(255) DEFAULT NULL COMMENT '备用字段',
  `filed1` varchar(255) DEFAULT NULL COMMENT '备用字段',
  `img1_small_x` varchar(255) DEFAULT NULL COMMENT 'iphonex适配图片',
  `img2_small_x` varchar(255) DEFAULT NULL COMMENT 'iphonex适配图片',
  `img3_small_x` varchar(255) DEFAULT NULL COMMENT 'iphonex适配图片',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `promotion_ad_SEG` (`promotionSpaceId`,`startTime`,`endTime`,`isDel`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `promotion_ad_temp` */

/*Table structure for table `promotion_space` */

DROP TABLE IF EXISTS `promotion_space`;

CREATE TABLE `promotion_space` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `spaceKey` varchar(255) DEFAULT NULL COMMENT '广告位key',
  `createTime` datetime DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `isDel` int(2) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `promotion_space_key_isDel` (`spaceKey`,`isDel`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=184 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `promotion_space` */

insert  into `promotion_space`(`id`,`name`,`spaceKey`,`createTime`,`updateTime`,`isDel`) values 
(1,'首页顶部推荐位1','666','2020-07-14 13:03:31','2020-08-28 16:01:09',0),
(2,'首页侧边广告位','888','2020-07-14 13:03:31','2020-07-17 11:53:02',0),
(3,'首页顶部轮播','999','2020-07-14 13:03:31','2020-07-17 13:13:03',0),
(160,'sadfa','123','2020-07-14 13:03:31','2020-08-31 18:50:00',0),
(161,'ffff','456','2020-07-14 13:03:31','2020-07-17 11:21:26',0),
(162,'广告名称','789','2020-07-14 13:03:31','2020-07-14 13:03:11',0),
(163,'广告名称','78911111','2020-07-14 13:03:31','2020-07-14 13:03:11',0),
(164,'广告名称','78911111','2020-07-14 13:03:31','2020-07-14 13:03:11',0),
(165,'广告名称','78911111','2020-07-14 13:03:31','2020-07-14 13:03:11',0),
(166,'广告名称','78911111','2020-07-14 13:03:31','2020-07-14 13:03:11',0),
(167,'广告名称','78911111','2020-07-14 13:03:31','2020-07-14 13:03:11',0),
(168,'广告名称111','33333','2020-07-14 13:03:31','2020-07-14 13:03:49',0),
(169,'名称','abcd','2020-07-14 16:30:38','2020-07-14 16:31:31',0),
(170,'冰淇淋套餐',NULL,'2020-07-14 17:39:19','2020-07-14 17:40:24',0),
(171,'12111111',NULL,'2020-07-15 12:18:47','2020-07-15 12:19:06',0),
(172,'边栏广告',NULL,'2020-08-07 14:32:51','2020-08-07 14:33:21',0),
(173,'测试广告位',NULL,NULL,NULL,0),
(174,'测试广告位11',NULL,NULL,NULL,0),
(175,'测试广告位3333','1596804347467','2020-08-07 07:45:47','2020-08-07 08:02:08',0),
(176,'aabbbaaa','1596808822338','2020-08-07 22:00:22','2020-08-10 15:47:03',0),
(177,'测试广告位',NULL,'2020-08-28 15:00:28','2020-08-28 15:00:28',0),
(178,'测试添加广告位',NULL,'2020-08-28 15:02:41','2020-08-28 15:02:41',0),
(179,'测试添加广告位1111',NULL,'2020-08-28 15:07:26','2020-08-28 15:07:26',0),
(180,'测试添加广告位名称',NULL,'2020-08-28 15:25:30','2020-08-28 15:25:30',0),
(181,'吃1',NULL,'2020-09-15 18:22:51','2020-09-15 18:22:51',0),
(182,'111',NULL,'2020-09-28 10:35:50','2020-09-28 10:35:50',0),
(183,'123',NULL,'2020-09-28 18:13:00','2020-09-28 18:13:00',0);

/*Table structure for table `search_position` */

DROP TABLE IF EXISTS `search_position`;

CREATE TABLE `search_position` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) NOT NULL COMMENT '关键词',
  `synonyms` varchar(255) DEFAULT NULL COMMENT '同义词',
  `isdel` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='广告搜索同义词表';

/*Data for the table `search_position` */

/*Table structure for table `side_b_operate_log` */

DROP TABLE IF EXISTS `side_b_operate_log`;

CREATE TABLE `side_b_operate_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fId` int(11) DEFAULT NULL COMMENT '关联id',
  `fType` varchar(255) DEFAULT NULL COMMENT '日志类型',
  `operateName` varchar(255) DEFAULT NULL,
  `operateDetail` varchar(2048) DEFAULT NULL,
  `operator` varchar(255) DEFAULT NULL,
  `createTime` datetime DEFAULT NULL,
  `isDel` int(11) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='系统日志';

/*Data for the table `side_b_operate_log` */

/*Table structure for table `side_b_recent_news` */

DROP TABLE IF EXISTS `side_b_recent_news`;

CREATE TABLE `side_b_recent_news` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `url` varchar(255) NOT NULL COMMENT '文章链接',
  `OnlineTime` datetime NOT NULL COMMENT '上线时间',
  `topTime` datetime DEFAULT NULL COMMENT '置顶时间',
  `offlineTime` datetime DEFAULT NULL COMMENT '下线时间',
  `updateTime` datetime DEFAULT NULL COMMENT '更新时间',
  `isDel` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否删除 1 删除 默认0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `side_b_recent_news` */

/*Table structure for table `special_ad` */

DROP TABLE IF EXISTS `special_ad`;

CREATE TABLE `special_ad` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT '广告名称',
  `backgroundImg` varchar(256) NOT NULL COMMENT '专场广告url',
  `description` varchar(64) NOT NULL COMMENT '描述',
  `hotLayout` varchar(32) NOT NULL DEFAULT '2X4' COMMENT '布局',
  `disable` int(1) DEFAULT '0' COMMENT '是否启用，1 不可用 0 可用 默认0 ',
  `startTime` datetime DEFAULT NULL COMMENT '开始时间',
  `endTime` datetime DEFAULT NULL COMMENT '结束时间',
  `subsite` varchar(255) DEFAULT NULL COMMENT '分站',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` varchar(128) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `isDel` int(1) DEFAULT '0' COMMENT '是否删除，1 已删除 0 未删除 默认0 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='专场广告表';

/*Data for the table `special_ad` */

/*Table structure for table `special_ad_hot` */

DROP TABLE IF EXISTS `special_ad_hot`;

CREATE TABLE `special_ad_hot` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `specialAdId` int(11) NOT NULL COMMENT '专场广告ID',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `url` varchar(256) NOT NULL COMMENT '热点链接',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='专场广告热区信息表';

/*Data for the table `special_ad_hot` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
