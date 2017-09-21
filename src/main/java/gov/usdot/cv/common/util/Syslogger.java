package gov.usdot.cv.common.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SyslogAppender;

/**
 * Singleton syslog logger. Encapsulates underlying implementation. Logs at all levels.
 * 
 * Self-contained; will not log to any other appenders/loggers.
 * 
 * @author vernona
 */
public class Syslogger {

	private static final String patternLayout = "%d [%p] %m%n";
	private static final String format = "Connected Vehicle [%s] component report: %s";

	private SyslogAppender appender;
	private Logger logger;

	/** The singleton instance. */
	private static final Syslogger instance = new Syslogger();

	/**
	 * Get the singleton instance.
	 * 
	 * @return
	 */
	public static Syslogger getInstance() {
		return instance;
	}

	/**
	 * Private constructor enforces the singleton pattern.
	 */
	private Syslogger() {
		appender = new SyslogAppender(new PatternLayout(patternLayout), "localhost:514", SyslogAppender.LOG_SYSLOG);
		appender.setName("syslog");

		logger = Logger.getLogger(Syslogger.class);
		logger.setLevel(Level.TRACE);
		logger.addAppender(appender);
		logger.setAdditivity(false); // Don't log to ancestor logs.
	}
	
	public void log(String identifier, String message) {
		info(String.format(format, identifier, message));
	}

	protected void trace(String message) {
		logger.trace(message);
	}

	protected void debug(String message) {
		logger.debug(message);
	}

	protected void info(String message) {
		logger.info(message);
	}

	protected void warn(String message) {
		logger.warn(message);
	}

	protected void error(String message) {
		logger.error(message);
	}

	protected void fatal(String message) {
		logger.fatal(message);
	}
	
	public static void main(String[] args) {
		Syslogger syslogger = Syslogger.getInstance();
		syslogger.log("Test main", "test message");
	}
}
