package cn.csservice.dataexchange.Entity;

import java.util.Date;

public class SqlRecord {

	/** 主键 */
	private int id;
	/** 执行的sql语句 */
	private String sqlsentence;
	/** 记录sql的时间 */
	private Date crTime;
	/** sql的执行状态，0-未执行，1-已执行 */
	private int status;

	public SqlRecord() {
	}

	public SqlRecord(int id, String sqlsentence, Date crTime, int status) {
		this.id = id;
		this.sqlsentence = sqlsentence;
		this.crTime = crTime;
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSqlsentence() {
		return sqlsentence;
	}

	public void setSqlsentence(String sqlsentence) {
		this.sqlsentence = sqlsentence;
	}

	public Date getCrTime() {
		return crTime;
	}

	public void setCrTime(Date crTime) {
		this.crTime = crTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
