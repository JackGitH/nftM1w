CREATE TABLE `account` (
                           `pk` varchar(255) NOT NULL,
                           `pub` varchar(255) DEFAULT NULL,
                           `status` smallint(255) DEFAULT NULL COMMENT '1 正常 2 冻结',
                           `modtime` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '修改时间',
                           `createtime` timestamp NULL DEFAULT current_timestamp() COMMENT '创建时间',
                           PRIMARY KEY (`pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `airdrop` (
                           `id` bigint(11) NOT NULL AUTO_INCREMENT,
                           `chainid` bigint(11) DEFAULT NULL,
                           `fromaddress` varchar(255) DEFAULT NULL,
                           `toaddress` varchar(255) DEFAULT NULL,
                           `amount` varchar(255) DEFAULT NULL,
                           `contractname` varchar(255) DEFAULT NULL,
                           `contractaddress` varchar(255) DEFAULT NULL,
                           `txhash` varchar(255) DEFAULT NULL,
                           `datetime` timestamp NULL DEFAULT NULL,
                           `txstatus` smallint(6) DEFAULT NULL COMMENT '1 pending 2 success 3 fail',
                           `modtime` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '修改时间',
                           `createtime` timestamp NULL DEFAULT current_timestamp() COMMENT '创建时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8;


CREATE TABLE `chain` (
                         `id` bigint(255) NOT NULL,
                         `tokenname` varchar(255) DEFAULT NULL,
                         `url` varchar(255) DEFAULT NULL COMMENT '链 url',
                         `apikey` varchar(255) DEFAULT NULL COMMENT '注册账号后区块链浏览器的apikey',
                         `status` smallint(6) DEFAULT NULL COMMENT '1 正常2 冻结',
                         `rate` varchar(255) DEFAULT NULL COMMENT '费率',
                         `modtime` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '修改时间',
                         `createtime` timestamp NULL DEFAULT current_timestamp() COMMENT '创建时间',
                         `priceurl` varchar(255) DEFAULT NULL COMMENT '获取gasprice的接口地址',
                         `recipientaddress` varchar(255) DEFAULT NULL,
                         `chainname` varchar(255) DEFAULT NULL COMMENT '链名',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
