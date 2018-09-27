CREATE TABLE sqlrecord (
	id int(11) PRIMARY KEY AUTO_INCREMENT,
	sqlsentence text,
	crtime datetime DEFAULT NULL,
	status tinyint(4) DEFAULT '0'
);