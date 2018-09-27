package cn.csservice.dataexchange.dao;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class BaseDao extends JdbcDaoSupport {

	private static Logger logger = Logger.getLogger(BaseDao.class);

	/**
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 */
	public void update(String sql, Object[] args, int[] argTypes) {
		if (sql == null || "".equals(sql.trim())) {
			logger.error("sql为空，不执行sql，直接返回");
			return;
		}
		if (args == null || args.length == 0 || argTypes == null || argTypes.length == 0) {
			logger.info("参数为空，将只使用sql参数来执行");
			update(sql);
			return;
		}
		this.getJdbcTemplate().update(sql, args, argTypes);
	}

	/**
	 * 
	 * @param sql
	 */
	public void update(String sql) {
		if (sql == null || "".equals(sql.trim())) {
			logger.error("sql为空，不执行sql，直接返回");
			return;
		}
		this.getJdbcTemplate().update(sql);
	}
}
