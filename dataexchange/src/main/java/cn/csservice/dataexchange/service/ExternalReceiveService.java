package cn.csservice.dataexchange.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cn.csservice.dataexchange.dao.BaseDao;
import cn.csservice.dataexchange.dao.SqlRecordDao;
import cn.csservice.dataexchange.tools.StringUtils;

public class ExternalReceiveService {
	private static Logger logger = Logger.getLogger(ExternalReceiveService.class);
	/**
	 * 信任ip列表
	 */
	private String trustIps;
	/**
	 * 可接收的表名
	 */
	private String tables;
	/**
	 * 内外网同时操作的表名
	 */
	private String opTables;
	/**
	 * 外网只存储简项数据的表名
	 */
	private String simpleTables;
	/**
	 * 外网只存储简项数据的字段名
	 */
	private String simpleTableColumns;
	/**
	 * 外网只存储简项数据的字段名Map
	 */
	private Map<String, String> simpleTableColumnMap = new HashMap<String, String>();
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

	public String getSimpleTables() {
		return simpleTables;
	}

	public void setSimpleTables(String simpleTables) {
		this.simpleTables = simpleTables;
	}

	public String getSimpleTableColumns() {
		return simpleTableColumns;
	}

	public void setSimpleTableColumns(String simpleTableColumns) {
		this.simpleTableColumns = simpleTableColumns;
		String[] arrTableColumns = simpleTableColumns.split(";");
		for (String tableColumns : arrTableColumns) {
			if (tableColumns.contains(":")) {
				String tableName = tableColumns.substring(0, tableColumns.indexOf(':'));
				String columns = tableColumns.substring(tableColumns.indexOf(':') + 1);
				simpleTableColumnMap.put(tableName, columns);
			}
		}
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
		return StringUtils.isIncluded(tables, tableName);
	}

	/**
	 * 外网接收到从内网传过来的sql,对于insert操作，需要做字段排除操作，只保留简项数据
	 * 对于update操作，set后面的操作部分，也需要做字段排除操作，where后面的条件部分不处理 对于delete操作，保持原样执行
	 * 对于内外网同时操作的表，需要记录一下接收到的sql,不能将接收的sql,再传给内网去执行
	 *
	 * @param sql
	 */
	public void processSql(String sql) {
		// 先判断表名，是否是内外网同时操作的表，如果是，需要记录一下执行的SQL
		// 然后在监控日志模块，将记录的sql排除掉
		String tableName = StringUtils.matchSql(sql);
		boolean bBoth = StringUtils.isIncluded(opTables, tableName);
		if (bBoth) {
			sqlRecordDao.insert(sql, new Date(), 0);
		}
		// 删除语句，直接执行
		if (sql.startsWith("delete")) {
			baseDao.update(sql);
			// 插入语句，将不必要的字段名和字段值都去掉，重新拼一个sql，再执行
		} else if (sql.startsWith("insert")) {
			if (StringUtils.isIncluded(simpleTables, tableName)) {
				Map<String, String[]> sqlDataMap = StringUtils.getDataFromSql(sql);
				String finalSql = StringUtils.getInsertSql(tableName, sqlDataMap, simpleTableColumnMap);
				baseDao.update(finalSql);
			} else {
				baseDao.update(sql);
			}
			// 修改语句，将不必要的字段名和字段值从set中去掉，where里的条件需要保留
		} else if (sql.startsWith("update")) {
			if (StringUtils.isIncluded(simpleTables, tableName)) {
				Map<String, String[]> sqlDataMap = StringUtils.getDataFromSql(sql);
				String finalSql = StringUtils.getUpdateSql(sql, tableName, sqlDataMap, simpleTableColumnMap);
				if ("".equals(finalSql.trim())) {
					logger.error("update语句所修改的字段都是非简项字段，此sql不执行，sql[" + sql + "]");
				} else {
					baseDao.update(finalSql);
				}
			} else {
				baseDao.update(sql);
			}
		}
	}

}
