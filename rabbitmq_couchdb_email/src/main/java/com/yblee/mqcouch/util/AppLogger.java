package com.yblee.mqcouch.util;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class AppLogger {
	private static final Logger logger = Logger.getLogger(AppLogger.class);

    public static void tieSystemOutAndErrToLog() {
    	System.setOut(createLoggingProxy(System.out));
        System.setErr(createLoggingProxy(System.err));
    }
    
    public static void initialize(String strLogFileName) {
    	RollingFileAppender loggerAppender = null;
    	try {
    		loggerAppender = new RollingFileAppender(new PatternLayout("%d - %p - %m%n"), "log/" + (strLogFileName.isEmpty()?"app":strLogFileName) + ".log", true);
    		loggerAppender.setMaxFileSize("1MB");
    		loggerAppender.setMaxBackupIndex(10);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	logger.setLevel(Level.INFO);
    	logger.addAppender(loggerAppender);
    	
    	Logger.getLogger("org.apache").setLevel(Level.OFF);
    }

    private static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                realPrintStream.print(string);
                logger.info(string);
            }
        };
    }
}

