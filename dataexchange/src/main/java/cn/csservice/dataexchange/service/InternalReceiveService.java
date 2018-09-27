package cn.csservice.dataexchange.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cn.csservice.dataexchange.dao.BaseDao;
import cn.csservice.dataexchange.dao.SqlRecordDao;
import cn.csservice.dataexchange.tools.StringUtils;

public class InternalReceiveService {
	private static Logger logger = Logger.getLogger(InternalReceiveService.class);
	private String trustIps;
	private String tables;
	private String opTables;
	@Autowired
	BaseDao baseDao;
	@Autowired
	SqlRecordDao sqlRecordDao;
	public String getTrustIps() {
		return trustIps;
	}

	public void setTrustIps(String trustIps) {
		this.trustIps = trustIps;
	}

	public String getTables() {
		return tables;
	}

	public void setTables(String tables) {
		this.tables = tables;
	}

	public String getOpTables() {
		return opTables;
	}

	public void setOpTables(String opTables) {
		this.opTables = opTables;
	}

	/**
	 * 判断是否在信任ip列表中
	 * 
	 * @param clientIp
	 * @return
	 */
	public boolean isTrusted(String clientIp) {
		return StringUtils.isIncluded(trustIps, clientIp);
	}

	/**
	 * 判断sql所操作的表，是否在可接收的列表中
	 * 
	 * @param sql
	 * @return
	 */
	public boolean isReceivableTable(String sql) {
		if (sql == null || "".equals(sql.trim())) {
			return false;
		}
		String tableName = StringUtils.matchSql(sql);
		logger.info("tables=["+tables+"]");
		logger.info("tableName=["+tableName+"]");
		return StringUtils.isIncluded(tables, tableName);
	}

	/**
	 * 内网接收到从外网传过来的sql,基本上保持原样执行
	 * 对于内外网同时操作的表，需要记录一下接收到的sql,不能将接收的sql,再传给外网去执行
	 *
	 * @param sql
	 */
	public void processSql(String sql) {
		//先判断表名，是否是内外网同时操作的表，如果是，需要记录一下执行的SQL
		//然后在监控日志模块，将记录的sql排除掉
		String tableName = StringUtils.matchSql(sql);
		boolean bBoth = StringUtils.isIncluded(opTables, tableName);
		if (bBoth) {
			sqlRecordDao.insert(sql, new Date(), 0);
		}
		baseDao.update(sql);
	}
}
