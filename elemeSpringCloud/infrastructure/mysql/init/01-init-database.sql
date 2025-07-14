-- 创建 nacos 配置数据库
CREATE DATABASE IF NOT EXISTS nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 创建 nacos 用户
CREATE USER IF NOT EXISTS 'nacos'@'%' IDENTIFIED BY 'nacos123';
GRANT ALL PRIVILEGES ON nacos_config.* TO 'nacos'@'%';

-- 创建 eleme 业务数据库
CREATE DATABASE IF NOT EXISTS eleme_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 创建 eleme 用户
CREATE USER IF NOT EXISTS 'eleme'@'%' IDENTIFIED BY 'eleme123';
GRANT ALL PRIVILEGES ON eleme_db.* TO 'eleme'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 使用 nacos_config 数据库
USE nacos_config;

-- 创建 nacos 配置表
CREATE TABLE IF NOT EXISTS `config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) DEFAULT NULL,
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) DEFAULT NULL,
  `c_use` varchar(64) DEFAULT NULL,
  `effect` varchar(64) DEFAULT NULL,
  `type` varchar(64) DEFAULT NULL,
  `c_schema` text,
  `encrypted_data_key` text NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info';

-- 创建其他 nacos 相关表
CREATE TABLE IF NOT EXISTS `config_info_aggr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) NOT NULL COMMENT 'datum_id',
  `content` longtext NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='增加租户字段';

-- 使用 eleme_db 数据库创建业务表
USE eleme_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `phone_number` varchar(20) NOT NULL COMMENT '用户手机号',
  `password` varchar(255) NOT NULL COMMENT '用户密码',
  `gender` varchar(10) DEFAULT NULL COMMENT '用户性别',
  `name` varchar(100) DEFAULT NULL COMMENT '用户姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '用户邮箱',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(1) DEFAULT 1 COMMENT '用户状态(0-禁用,1-正常)',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像URL',
  PRIMARY KEY (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 创建商家表
CREATE TABLE IF NOT EXISTS `business` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `password` varchar(255) DEFAULT NULL COMMENT '商家登录密码',
  `business_name` varchar(100) NOT NULL COMMENT '商家名称',
  `rating` varchar(10) DEFAULT NULL COMMENT '商家评分',
  `sales` varchar(50) DEFAULT NULL COMMENT '商家销量',
  `distance` varchar(50) DEFAULT NULL COMMENT '距离、时间',
  `min_order` varchar(50) DEFAULT NULL COMMENT '起送价格',
  `comment` varchar(255) DEFAULT NULL COMMENT '评价',
  `discounts` text COMMENT '折扣、满减',
  `discount` varchar(100) DEFAULT NULL COMMENT '店内显示折扣',
  `notice` text COMMENT '公告',
  `sidebar_items` text COMMENT '侧栏元素',
  `img_logo` varchar(255) DEFAULT NULL COMMENT '商家LOGO图片地址',
  `delivery` varchar(50) DEFAULT NULL COMMENT '配送费',
  `type` varchar(50) DEFAULT NULL COMMENT '商家类型',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(1) DEFAULT 1 COMMENT '商家状态(0-禁用,1-正常)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家表';

-- 创建食物表
CREATE TABLE IF NOT EXISTS `food` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '食物ID',
  `name` varchar(100) NOT NULL COMMENT '食物名称',
  `text` text COMMENT '食物描述',
  `amount` varchar(50) DEFAULT NULL COMMENT '销量',
  `discount` varchar(100) DEFAULT NULL COMMENT '打折信息',
  `red_price` decimal(10,2) DEFAULT NULL COMMENT '现价',
  `gray_price` varchar(50) DEFAULT NULL COMMENT '原价',
  `business` int(11) NOT NULL COMMENT '所属商家ID',
  `img` varchar(255) DEFAULT NULL COMMENT '食物图片路径',
  `selling` tinyint(1) DEFAULT 1 COMMENT '是否上架(0-下架,1-上架)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(1) DEFAULT 1 COMMENT '食物状态(0-禁用,1-正常)',
  `category` varchar(50) DEFAULT NULL COMMENT '食物分类',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business`),
  KEY `idx_selling` (`selling`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='食物表';

-- 创建用户订单表
CREATE TABLE IF NOT EXISTS `user_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `business_id` int(11) NOT NULL COMMENT '商家ID',
  `user_phone` varchar(20) NOT NULL COMMENT '用户手机号',
  `order_list` text NOT NULL COMMENT '订单商品列表',
  `price` decimal(10,2) NOT NULL COMMENT '订单总价',
  `state` tinyint(1) DEFAULT 0 COMMENT '订单状态(0-未支付,1-已支付,2-已完成,3-已取消)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `delivery_address` text COMMENT '收货地址',
  `receiver_name` varchar(100) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人电话',
  `remark` text COMMENT '备注',
  `delivery_fee` decimal(10,2) DEFAULT 0.00 COMMENT '配送费',
  PRIMARY KEY (`id`),
  KEY `idx_business_id` (`business_id`),
  KEY `idx_user_phone` (`user_phone`),
  KEY `idx_state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户订单表'; 