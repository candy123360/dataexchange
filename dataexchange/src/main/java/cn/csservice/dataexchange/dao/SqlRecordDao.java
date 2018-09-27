package cn.csservice.dataexchange.dao;

import java.util.Date;
import java.util.List;

import cn.csservice.dataexchange.Entity.SqlRecord;
import cn.csservice.dataexchange.Entity.SqlRecordRowMapper;

public class SqlRecordDao extends BaseDao {

	/**
	 * 查询sql记录
	 * 
	 * @param sqlsentence
	 * @param status
	 * @return
	 */
	public List<SqlRecord> Query(String sqlsentence, int status) {
		String sql = "select * from sqlrecord where sqlsentence=? and status=?";
		List<SqlRecord> sqlList = getJdbcTemplate().query(sql, new Object[] { sqlsentence, status },
				new SqlRecordRowMapper());
		return sqlList;
	}

	/**
	 * 修改sql记录的状态，0-未执行，1-已执行
	 * 
	 * @param id
	 * @param status
	 */
	public void update(int id, int status) {
		String sql = "update sqlrecord set status=? where id=?";
		getJdbcTemplate().update(sql, status, id);
	}

	/**
	 * 插入sql记录
	 * 
	 * @param sqlsentence
	 * @param status
	 */
	public void insert(String sqlsentence, Date crTime, int status) {
		String sql = "insert into sqlrecord (sqlsentence,crtime,status) values (?,?,?)";
		getJdbcTemplate().update(sql, sqlsentence, crTime, status);
	}
}
