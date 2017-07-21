CREATE TABLE IF NOT EXISTS `lts_admin_node_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) DEFAULT NULL,
  `config_value` varchar(255) DEFAULT NULL,
  `node_type` varchar(255) DEFAULT NULL,
  `node_group` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8