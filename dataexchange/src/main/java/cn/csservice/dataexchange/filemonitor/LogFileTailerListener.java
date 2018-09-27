package cn.csservice.dataexchange.filemonitor;

public interface LogFileTailerListener {
	public abstract void newLogFileLine(String line);
}
