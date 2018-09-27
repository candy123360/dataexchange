package cn.csservice.dataexchange.filemonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cn.csservice.dataexchange.Entity.SqlRecord;
import cn.csservice.dataexchange.dao.SqlRecordDao;
import cn.csservice.dataexchange.tools.HttpUtils;
import cn.csservice.dataexchange.tools.StringUtils;

public class LogFileTail implements LogFileTailerListener {
	private static Logger logger = Logger.getLogger(LogFileTail.class);
	private String url;
	private String paramName;
	private String sendableTables;
	private String opTables;
	private Set<String> transactionIdSet = new HashSet<String>();
	private Map<String, List<String>> transactionSqlMap = new HashMap<String, List<String>>();
	@Autowired
	SqlRecordDao sqlRecordDao;

	public void newLogFileLine(String line) {
		String[] content = line.split("\t");
		// Mysql5.1和5.7的日志格式不一样，5.1分四段，5.7分三段
		if (content.length >= 3) {
			if (content[1].contains("Prepare") || content[2].contains("Prepare")) {
				return;
			}
			// 事务ID
			String transactionId = content[1].trim().toLowerCase();
			if (content.length == 4) {
				transactionId = content[2].trim().toLowerCase();
			}
			if(!"".equals(transactionId.trim())||transactionId.contains(" ")){
				transactionId = transactionId.substring(0, transactionId.indexOf(' '));
			}else{
				transactionId="";
			}
			// 执行的sql语句
			String sSql = content[2].trim().toLowerCase();
			if (content.length == 4) {
				sSql = content[3].trim().toLowerCase();
			}
			// 如果是set autocommit=0，说明是开启事务，将事务ID放入set中，遇到commit和rollback，清除掉事务ID
			if ("set autocommit=0".equals(sSql)) {
				if(!"".equals(transactionId.trim())){
					transactionIdSet.add(transactionId);
				}
			}
			if ("commit".equals(sSql)) {
				transactionIdSet.remove(transactionId);
				// 遇到commit，将map里的sql列表发送出去，然后将事务ID从map里清除掉
				List<String> sqlList = transactionSqlMap.remove(transactionId);
				if (sqlList != null) {
					for (String sql : sqlList) {
						logger.info("发送sql[" + sql + "]");
						// 发送httppost
						Map<String, String> params = new HashMap<String, String>();
						params.put(paramName, sql);
						HttpUtils.postJson(url, params);
					}
				}
			}
			if ("rollback".equals(sSql)) {
				transactionIdSet.remove(transactionId);
			}
			// 只发送insert,update和delete的sql，测试时，可先注释掉
			if (sSql.startsWith("insert") || sSql.startsWith("update") || sSql.startsWith("delete")) {
				// 判断sql所涉及的表名是否在可发送表名列表中
				String tableName = StringUtils.matchSql(sSql);
				boolean bSendable = StringUtils.isIncluded(sendableTables, tableName);
				if (bSendable) {
					boolean bBoth = StringUtils.isIncluded(opTables, tableName);
					// 判断是否是内外网同时操作的表
					if (bBoth) {
						List<SqlRecord> sqlList = sqlRecordDao.Query(sSql, 0);
						// 从数据库中查询是否记录了此sql
						if (!sqlList.isEmpty()) {
							SqlRecord sqlRecord = sqlList.get(0);
							sqlRecordDao.update(sqlRecord.getId(), 1);
							logger.info("此sql是从另一端接收的sql，不发送，sql[" + sSql + "]");
							return;
						}
					}
					if (transactionIdSet.contains(transactionId)) {
						List<String> sqlList = transactionSqlMap.get(transactionId);
						if (sqlList == null) {
							sqlList = new ArrayList<String>();
						}
						sqlList.add(sSql);
						transactionSqlMap.put(transactionId, sqlList);
					} else {
						logger.info("发送sql[" + sSql + "]");
						// 发送httppost
						Map<String, String> params = new HashMap<String, String>();
						params.put(paramName, sSql);
						HttpUtils.postJson(url, params);
					}
				} else {
					logger.info("sql所涉及表名不在可发送列表中，此sql暂不发送，sql[" + sSql + "]");
				}
			}
		}
	}

	public static void main(String[] args) {
		LogFileTailerListener logFileListener = new LogFileTail();
		LogFileTailer tailer = new LogFileTailer(new File("d:/tools/mysql11.log"), 1000, true);
		tailer.setTailing(true);
		tailer.addLogFileTailerListener(logFileListener);
		tailer.start();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getSendableTables() {
		return sendableTables;
	}

	public void setSendableTables(String sendableTables) {
		this.sendableTables = sendableTables;
	}

	public String getOpTables() {
		return opTables;
	}

	public void setOpTables(String opTables) {
		this.opTables = opTables;
	}

}