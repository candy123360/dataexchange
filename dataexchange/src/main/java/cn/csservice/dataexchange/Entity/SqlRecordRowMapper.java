package cn.csservice.dataexchange.Entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class SqlRecordRowMapper implements RowMapper<SqlRecord> {

	public SqlRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		SqlRecord sqlRecord = new SqlRecord(rs.getInt("id"), rs.getString("sqlsentence"), rs.getDate("crTime"),
				rs.getInt("status"));
		return sqlRecord;
	}

}
