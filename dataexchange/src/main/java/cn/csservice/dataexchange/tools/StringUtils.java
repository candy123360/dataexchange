package cn.csservice.dataexchange.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	public static final String KEY_COLUMN = "column";
	public static final String KEY_VALUE = "value";

	/**
	 * @param sql
	 *            lowcase
	 * @return
	 */
	public static String matchSql(String sql) {
		// SELECT 列名称 FROM 表名称
		// SELECT * FROM 表名称
		if (sql.startsWith("select")) {
			Matcher matcher = Pattern.compile("select\\s.+from\\s(.+)where\\s(.*)").matcher(sql);
			if (matcher.find()) {
				return matcher.group(1).trim();
			}
		}
		// INSERT INTO 表名称 VALUES (值1, 值2,....)
		// INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
		if (sql.startsWith("insert")) {
			Matcher matcher = Pattern.compile("insert\\sinto\\s(.+)\\((.*)\\)\\svalues\\s\\((.*)\\)").matcher(sql);
			if (matcher.find()) {
				return matcher.group(1).trim();
			}
		}
		// UPDATE 表名称 SET 列名称 = 新值 WHERE 列名称 = 某值
		if (sql.startsWith("update")) {
			Matcher matcher = Pattern.compile("update\\s(.+)set\\s(.+)where\\s(.*)").matcher(sql);
			if (matcher.find()) {
				return matcher.group(1).trim();
			}
		}
		// DELETE FROM 表名称 WHERE 列名称 = 值
		if (sql.startsWith("delete")) {
			Matcher matcher = Pattern.compile("delete\\sfrom\\s(.+)where\\s(.*)").matcher(sql);
			if (matcher.find()) {
				return matcher.group(1).trim();
			}
		}
		return null;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param data
	 * @return
	 */
	public static boolean isBlank(String data) {
		if (data == null || "".equals(data.trim())) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否在列表中
	 * 
	 * @param strList
	 * @param str
	 * @return
	 */
	public static boolean isIncluded(String strList, String str) {
		if (strList == null || "".equals(strList.trim()) || str == null || "".equals(str.trim())) {
			return false;
		}
		String strList2 = "," + strList + ",";
		String str2 = "," + str + ",";
		if (strList2.contains(str2)) {
			return true;
		}
		/*
		 * String[] arrTrustIps = strList.split(","); List<String> ipList =
		 * Arrays.asList(arrTrustIps); if (ipList.contains(str)) { return true;
		 * }
		 */
		return false;
	}

	public static Map<String, String[]> getDataFromSql(String sql) {
		Map<String, String[]> resultMap = new HashMap<String, String[]>();
		if (sql == null || "".equals(sql.trim())) {
			return resultMap;
		}
		// INSERT INTO 表名称 VALUES (值1, 值2,....)
		// INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
		if (sql.startsWith("insert")) {
			Matcher matcher = Pattern.compile("insert\\sinto\\s(.+)\\((.*)\\)\\svalues\\s\\((.*)\\)").matcher(sql);
			if (matcher.find()) {
				String[] arrColumn = matcher.group(2).trim().split(",");
				String[] arrValue = matcher.group(3).trim().split(",");
				resultMap.put(KEY_COLUMN, arrColumn);
				resultMap.put(KEY_VALUE, arrValue);
			}
		}
		// UPDATE 表名称 SET 列名称 = 新值 WHERE 列名称 = 某值
		if (sql.startsWith("update")) {
			Matcher matcher = Pattern.compile("update\\s(.+)set\\s(.+)where\\s(.*)").matcher(sql);
			if (matcher.find()) {
				String[] arrSet = matcher.group(2).trim().split(",");
				String[] arrColumn = new String[arrSet.length];
				String[] arrValue = new String[arrSet.length];
				for (int i = 0; i < arrSet.length; i++) {
					if (arrSet[i].contains("=")) {
						arrColumn[i] = arrSet[i].substring(0, arrSet[i].indexOf('=')).trim();
						arrValue[i] = arrSet[i].substring(arrSet[i].indexOf('=') + 1).trim();
					}
				}
				resultMap.put(KEY_COLUMN, arrColumn);
				resultMap.put(KEY_VALUE, arrValue);
			}
		}

		return resultMap;
	}

	/**
	 * @param tableName
	 * @param sqlDataMap
	 * @return
	 */
	public static String getInsertSql(String tableName, Map<String, String[]> sqlDataMap,
			Map<String, String> simpleTableColumnMap) {
		String[] arrColumn = sqlDataMap.get(StringUtils.KEY_COLUMN);
		String[] arrValue = sqlDataMap.get(StringUtils.KEY_VALUE);
		StringBuilder sbColumn = new StringBuilder();
		StringBuilder sbValue = new StringBuilder();
		String columns = simpleTableColumnMap.get(tableName);
		for (int i = 0; i < arrColumn.length; i++) {
			if (StringUtils.isIncluded(columns, arrColumn[i].trim())) {
				sbColumn.append(arrColumn[i]);
				sbColumn.append(",");
				sbValue.append(arrValue[i]);
				sbValue.append(",");
			}
		}
		if (sbColumn.length() > 0) {
			sbColumn = sbColumn.deleteCharAt(sbColumn.length() - 1);
		}
		if (sbValue.length() > 0) {
			sbValue = sbValue.deleteCharAt(sbValue.length() - 1);
		}
		String finalSql = "insert into " + tableName + " (" + sbColumn.toString() + ") values (" + sbValue.toString()
				+ ")";
		return finalSql;
	}

	public static String getUpdateSql(String sql, String tableName, Map<String, String[]> sqlDataMap,
			Map<String, String> simpleTableColumnMap) {
		String[] arrColumn = sqlDataMap.get(StringUtils.KEY_COLUMN);
		String[] arrValue = sqlDataMap.get(StringUtils.KEY_VALUE);
		StringBuilder sbSet = new StringBuilder();
		String columns = simpleTableColumnMap.get(tableName);
		for (int i = 0; i < arrColumn.length; i++) {
			if (StringUtils.isIncluded(columns, arrColumn[i].trim())) {
				sbSet.append(arrColumn[i]);
				sbSet.append("=");
				sbSet.append(arrValue[i]);
				sbSet.append(",");
			}
		}
		if (sbSet.length() > 0) {
			sbSet = sbSet.deleteCharAt(sbSet.length() - 1);
		} else {
			// 如果set的内容都是非简项字段，则返回空串
			return "";
		}
		// 截取原sql，从开头到set后的一个空格，加上拼好的set语句，再上从where前的一个空格到最后
		String finalSql = sql.substring(0, sql.indexOf("set") + 4) + sbSet.toString()
				+ sql.substring(sql.indexOf("where") - 1);
		return finalSql;
	}

	public static void main(String[] args) {
		getDataFromSql("insert into abc (a,b,c) values (1,2,3)");
	}
}
