package cn.csservice.dataexchange.processor;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

import cn.csservice.dataexchange.filemonitor.LogFileTailer;
import cn.csservice.dataexchange.filemonitor.LogFileTailerListener;

public class FileMonitorProcessor implements ApplicationListener<ApplicationEvent> {
	private static final Logger logger = LoggerFactory.getLogger(FileMonitorProcessor.class);

	private String fileName;

	private LogFileTailerListener logFileListener;

	public void onApplicationEvent(final ApplicationEvent applicationEvent) {
		try {
			// 只在初始化“根上下文”的时候执行
			if (applicationEvent.getSource() instanceof XmlWebApplicationContext) {
				if ("Root WebApplicationContext"
						.equals(((XmlWebApplicationContext) applicationEvent.getSource()).getDisplayName())) {
					LogFileTailer tailer = new LogFileTailer(new File(fileName), 1000, true);
					tailer.setTailing(true);
					tailer.addLogFileTailerListener(logFileListener);
					tailer.start();
				}
			}
		} catch (Exception e) {
			logger.error(
					"((XmlWebApplicationContext) applicationEvent.getSource()).getDisplayName() 执行失败，请检查Spring版本是否支持",
					e);
		}

	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public LogFileTailerListener getLogFileListener() {
		return logFileListener;
	}

	public void setLogFileListener(LogFileTailerListener logFileListener) {
		this.logFileListener = logFileListener;
	}

}
