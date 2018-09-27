package cn.csservice.dataexchange.filemonitor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class LogFileTailer extends Thread {
	private static Logger logger = Logger.getLogger(LogFileTailer.class);
	private long sampleInterval = 9000;

	private File logfile;

	private boolean startAtBeginning = false;

	private boolean tailing = false;

	private Set<LogFileTailerListener> listeners = new HashSet<LogFileTailerListener>();

	public LogFileTailer(File file) {
		this.logfile = file;
	}

	public LogFileTailer(File file, long sampleInterval, boolean startAtBeginning) {
		this.logfile = file;
		this.sampleInterval = sampleInterval;
		this.startAtBeginning = startAtBeginning;
	}

	public void addLogFileTailerListener(LogFileTailerListener l) {
		this.listeners.add(l);
	}

	public void removeLogFileTailerListener(LogFileTailerListener l) {
		this.listeners.remove(l);
	}

	protected void fireNewLogFileLine(String line) {
		for (Iterator<LogFileTailerListener> i = this.listeners.iterator(); i.hasNext();) {
			LogFileTailerListener l = i.next();
			l.newLogFileLine(line);
		}
	}

	public void stopTailing() {
		this.tailing = false;
	}

	public void run() {
		long filePointer = 0;

		if (!this.startAtBeginning) {
			filePointer = this.logfile.length();
		}

		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(logfile, "r");
			while (this.tailing) {
				long fileLength = this.logfile.length();
				if (fileLength < filePointer) {
					file.close();
					file = new RandomAccessFile(logfile, "r");
					filePointer = 0;
				}
				if (fileLength > filePointer) {

					file.seek(filePointer);
					String line = new String(file.readLine().getBytes("ISO-8859-1"), "utf-8");
					while (line != null) {
						try {
							if (filePointer > 0) {
								this.fireNewLogFileLine(line);
							}
						} catch (Exception e) {
							logger.error("处理文件失败", e);
						}
						line = file.readLine();
						if (line != null) {
							line = new String(line.getBytes("ISO-8859-1"), "utf-8");
						}
					}
					filePointer = file.getFilePointer();
				}
				sleep(this.sampleInterval);
			}

		} catch (IOException e) {
			logger.error("读取文件发生异常", e);
		} catch (InterruptedException e) {
			logger.error("线程被中断", e);
			Thread.currentThread().interrupt();
		} finally {
			try {
				file.close();
			} catch (IOException e) {
				logger.error("关闭文件失败", e);
			}
		}
	}

	public void setTailing(boolean tailing) {
		this.tailing = tailing;
	}

}