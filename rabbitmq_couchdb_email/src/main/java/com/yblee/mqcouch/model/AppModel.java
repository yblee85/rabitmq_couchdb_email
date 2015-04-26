package com.yblee.mqcouch.model;

public class AppModel {
	public static String version = "0.0.2";
	public static String rootDirName = "";
//	public static String queueDirName = "queue";
	
	public static String CONFIG_FILENAME = "config.ini";
	
	public static String DEFAULT_RABBITMQ_HOST = "127.0.0.1";
	public static String DEFAULT_RABBITMQ_QUEUE_NAME = "EMAIL_QUEUE";
	
	public static String DEFAULT_SERVER_COUCH_HOST = "127.0.0.1"; 
	public static String DEFAULT_SERVER_COUCH_PORT = "5984";
	public static String DEFAULT_SERVER_COUCH_DB = "rabbitmq_email";
	
	public static String RABBITMQ_HOST = "";
	public static String RABBITMQ_QUEUE_NAME = "";
	
	public static String SERVER_COUCH_HOST = ""; 
	public static String SERVER_COUCH_PORT = "";
	public static String SERVER_COUCH_ADDRESS = "";
	public static String SERVER_COUCH_DB = "";
	public static String SERVER_COUCH_DB_SEQUENCE = "";
	
	public static String SENDER_EMAIL = "";
	public static String SENDER_EMAIL_PASS = "";
	public static String SERVER_COUCH_USER = "";
	public static String SERVER_COUCH_PASS = "";
	
	// enabled check couchdb server feed
	private boolean disabledCouchFeed = false, enabledDebug = false;
	private long sequence = 0;
	
	public AppModel(boolean isDisabledCouchFeed, boolean enabledDebug) {
		
		// set mode if it uses couchdb
		setDisabledCouchFeed(isDisabledCouchFeed);
		
		setEnabledDebug(enabledDebug);
		
		// set couchdb sequence
		try{
			setSequence(Long.valueOf(AppModel.SERVER_COUCH_DB_SEQUENCE));
		} catch(Exception e) {}
		
	}
	

	public boolean isEnabledDebug() {
		return enabledDebug;
	}

	public void setEnabledDebug(boolean enabledDebug) {
		this.enabledDebug = enabledDebug;
	}

	public boolean isDisabledCouchFeed() {
		return disabledCouchFeed;
	}

	public void setDisabledCouchFeed(boolean disabledCouchFeed) {
		this.disabledCouchFeed = disabledCouchFeed;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}
	
}
