package com.yblee.mqcouch.index;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.yblee.mqcouch.control.AppControl;
import com.yblee.mqcouch.model.AppModel;
import com.yblee.mqcouch.util.AppLogger;
import com.yblee.mqcouch.util.AppUtilities;
import com.yblee.mqcouch.util.SimpleProtector;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;

// tested : rabbitmq 3.4.4 / couch : 1.3.1, 1.6.1 / java 7, 8 / ubuntu 12.04, windows 7
public class MqCouchEmailApp {
	private static String strDisabledCouchFeed = "";
	private static boolean enabledDebug = false;
	
    public static void main(String[] args) {
    	
    	// check arguments and 
    	checkArgsForOption(args);
    	
		try {
			System.out.println("os : " + System.getProperty("os.name"));
			System.out.println("app version : " + AppModel.version);
			
//			File f = new File(System.getProperty("java.class.path"));
//			File dir = f.getAbsoluteFile().getParentFile();
//			String rootPath = dir.toString();
//			AppModel.rootDirName = rootPath;
//			System.out.println("root dir path : " + rootPath);
			
			/** LOG4J **/
			AppLogger.initialize(MqCouchEmailApp.class.getSimpleName());
			AppLogger.tieSystemOutAndErrToLog();
			
			// read config file and initialize model static values
			initConfigAndAppVariables();
			
			RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
			final int runtimePid = Integer.parseInt(rt.getName().substring(0, rt.getName().indexOf("@")));

			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (getMonitoredVMs(runtimePid)) {
						javax.swing.SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								// start app
								startApp(strDisabledCouchFeed, enabledDebug);
							}
						}
						);
					} else {
						System.out.println("The " + this.getClass().getSimpleName() +  " application is already running.");
						System.exit(0);
					}
				}
			});
			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
    }
    
    private static void startApp(String strDisabledCouchFeed, boolean enabledDebug) {
    	boolean isDisabledCouchFeed = false;
    	try {
    		isDisabledCouchFeed = Boolean.valueOf(strDisabledCouchFeed);
    	} catch(Exception e) {}
    	AppModel mqCouchEmailModel = new AppModel(isDisabledCouchFeed, enabledDebug);
    	AppControl mqCouchEmailControl = new AppControl(mqCouchEmailModel);
    	
    }
    
    private static void checkArgsForOption(String[] args) {
    	if(args.length == 1 && (args[0].compareTo("-h") == 0 || args[0].compareTo("--h") == 0)) {
			// will give you instruction about password
    		String msg = 
    				"RabbitMQ / CouchDB / Email Application" + "\n" + 
    				"You need to install RabbitMQ before use this app" + "\n" +
    				"you can specify sender email and couchdb user" + "\n" +
    				"-encrypt <your email account password> will give you encrypted value (without <>)" + "\n" +
    				"in order to specify sender email" + "\n" +
    				"in config.ini add [sender_email]\\n<your email account> (without <>)" + "\n" +
    				"[sender_email_encrypted_pass]\\n<encrypted password> (without <>)" + "\n" +
    				"or [sender_email_raw_pass]\\n<not ecrypted password> (without <>)" + "\n" +
    				"in order to specify couchdb user" + "\n" +
    				"in config.ini add [couchdb_user]\\n<your couchdb user name> (without <>)" + "\n" +
    				"[couchdb_user_encrypted_pass]\\n<encrypted password> (without <>)" + "\n" +
    				"or [couchdb_user_raw_pass]\\n<not ecrypted password> (without <>)" + "\n";
			System.out.println(msg);
			System.exit(0);
    	}
    	
    	if(args.length == 2) {
    		if(args[0].compareTo("-encrypt") == 0 || args[0].compareTo("--encrypt") == 0) {
    			// will take second argument and give you encriped password
        		String rawPass = args[1].trim();
        		String encryptedPass = "";
        		try {
        			encryptedPass = SimpleProtector.encrypt(rawPass);
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
        		System.out.println("Encrypted Value : [" + encryptedPass + "]");
        		System.exit(0);
    		}
    	}
    }
    
    private static void initConfigAndAppVariables() {
		File f = new File(AppModel.CONFIG_FILENAME);
		if(!f.exists()) {
			_createDefaultConfigFile();
		}
		
		Map<String, Object> mapConfig = AppUtilities.readFileIntoMap(AppModel.CONFIG_FILENAME);
		
//		for(String key : mapConfig.keySet()) {
//			System.out.println(key + " : " + mapConfig.get(key).toString());
//		}
		
		_initAppModelFromConfigMap(mapConfig);
		_initEmailAccountFromConfigMap(mapConfig);
		_initCouchDBSetupFromConfigMap(mapConfig);			
		_initRabbitMQSetupFromConfigMap(mapConfig);
				
	}
    
    private static void _initAppModelFromConfigMap(Map<String, Object> mapConfig) {
    	// check couch feed check
		if(mapConfig.containsKey("disable_couch_feed") && mapConfig.get("disable_couch_feed")!=null) {
			try {
				String strVal = mapConfig.get("disable_couch_feed").toString().trim();
				if(!strVal.isEmpty()) {
					strDisabledCouchFeed = strVal;
				}
			} catch(Exception e) {}
		}
		if(strDisabledCouchFeed.isEmpty()) {
			strDisabledCouchFeed = "false";
		}
		
		// check debug mode
		if(mapConfig.containsKey("debug") && mapConfig.get("debug")!=null) {
			try {
				String strVal = mapConfig.get("debug").toString().trim();
				if(!strVal.isEmpty()) {
					enabledDebug = Boolean.valueOf(strVal);
				}
			} catch(Exception e) {}
		}
		
    }
    
    private static void _initEmailAccountFromConfigMap(Map<String, Object> mapConfig) {
    	// check sender email
		if(mapConfig.containsKey("sender_email") && mapConfig.get("sender_email")!=null) {
			try {
				String sender_email = mapConfig.get("sender_email").toString().trim();
				if(!sender_email.isEmpty()) {
					AppModel.SENDER_EMAIL = sender_email;
				}
			} catch(Exception e) {}
		}
		if(mapConfig.containsKey("sender_email_raw_pass") && mapConfig.get("sender_email_raw_pass")!=null) {
			try {
				String sender_email_raw_pass = mapConfig.get("sender_email_raw_pass").toString().trim();
				if(!sender_email_raw_pass.isEmpty()) {
					AppModel.SENDER_EMAIL_PASS = sender_email_raw_pass;
				}
			} catch(Exception e) {}
		}
		if(mapConfig.containsKey("sender_email_encrypted_pass") && mapConfig.get("sender_email_encrypted_pass")!=null) {
			try {
				String sender_email_encrypted_pass = mapConfig.get("sender_email_encrypted_pass").toString().trim();
				if(!sender_email_encrypted_pass.isEmpty()) {
					String pass = SimpleProtector.decrypt(sender_email_encrypted_pass);
					AppModel.SENDER_EMAIL_PASS = pass;
				}
			} catch(Exception e) {}
		}
		if(AppModel.SENDER_EMAIL.isEmpty() || AppModel.SENDER_EMAIL_PASS.isEmpty()) {
			System.out.println("set sender email account. please, check -h");
			System.exit(0);
		}
    }
    
    private static void _initCouchDBSetupFromConfigMap(Map<String, Object> mapConfig) {
		// check couch host
		if(mapConfig.containsKey("couch_host") && mapConfig.get("couch_host")!=null) {
			try {
				String couch_host = mapConfig.get("couch_host").toString().trim();
				if(!couch_host.isEmpty()) {
					AppModel.SERVER_COUCH_HOST = couch_host;
				}
			} catch(Exception e) {}
		}
		if(AppModel.SERVER_COUCH_HOST.isEmpty()) {
			AppModel.SERVER_COUCH_HOST = AppModel.DEFAULT_SERVER_COUCH_HOST;
		}
		
		// check couch port
		if(mapConfig.containsKey("couch_port") && mapConfig.get("couch_port")!=null) {
			try {
				String port = mapConfig.get("couch_port").toString().trim();
				if(port.length()<=4) {
					AppModel.SERVER_COUCH_PORT = port;
				}
			} catch(Exception e) {}
		}
		if(AppModel.SERVER_COUCH_PORT.isEmpty()) {
			AppModel.SERVER_COUCH_PORT = AppModel.DEFAULT_SERVER_COUCH_PORT;
		}
		
		// check couch db name
		if(mapConfig.containsKey("couch_db") && mapConfig.get("couch_db")!=null) {
			try {
				String couch_db = mapConfig.get("couch_db").toString().trim();
				if(!couch_db.isEmpty()) {
					AppModel.SERVER_COUCH_DB = couch_db;
				}
			} catch(Exception e) {}
		}
		if(AppModel.SERVER_COUCH_DB.isEmpty()) {
			AppModel.SERVER_COUCH_DB = AppModel.DEFAULT_SERVER_COUCH_DB;
		}
		
		// check couch user account
		if(mapConfig.containsKey("couchdb_user") && mapConfig.get("couchdb_user")!=null) {
			try {
				String couchdb_user = mapConfig.get("couchdb_user").toString().trim();
				if(!couchdb_user.isEmpty()) {
					AppModel.SERVER_COUCH_USER = couchdb_user;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(mapConfig.containsKey("couchdb_user_raw_pass") && mapConfig.get("couchdb_user_raw_pass")!=null) {
			try {
				String couchdb_user_raw_pass = mapConfig.get("couchdb_user_raw_pass").toString().trim();
				if(!couchdb_user_raw_pass.isEmpty()) {
					AppModel.SERVER_COUCH_PASS = couchdb_user_raw_pass;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(mapConfig.containsKey("couchdb_user_encrypted_pass") && mapConfig.get("couchdb_user_encrypted_pass")!=null) {
			try {
				String couchdb_user_encrypted_pass = mapConfig.get("couchdb_user_encrypted_pass").toString().trim();
				if(!couchdb_user_encrypted_pass.isEmpty()) {
					String pass = SimpleProtector.decrypt(couchdb_user_encrypted_pass);
					AppModel.SERVER_COUCH_PASS = pass;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// check couch db sequence
		if(mapConfig.containsKey("couch_db_sequence") && mapConfig.get("couch_db_sequence")!=null) {
			try {
				String couch_db_sequence = mapConfig.get("couch_db_sequence").toString().trim();
				if(!couch_db_sequence.isEmpty()) {
					AppModel.SERVER_COUCH_DB_SEQUENCE = couch_db_sequence;
				}
			} catch(Exception e) {}
		}
		
		// set server address
		AppModel.SERVER_COUCH_ADDRESS = "http://" + AppModel.SERVER_COUCH_HOST + ":"+ AppModel.SERVER_COUCH_PORT;
    }
    
    private static void _initRabbitMQSetupFromConfigMap(Map<String, Object> mapConfig) {
    	// check rabbitmq queue name
		if(mapConfig.containsKey("mq_queue_name") && mapConfig.get("mq_queue_name")!=null) {
			try {
				String mq_queue_name = mapConfig.get("mq_queue_name").toString().trim();
				if(!mq_queue_name.isEmpty()) {
					AppModel.RABBITMQ_QUEUE_NAME = mq_queue_name;
				}
			} catch(Exception e) {}
		}
		if(AppModel.RABBITMQ_QUEUE_NAME.isEmpty()) {
			AppModel.RABBITMQ_QUEUE_NAME = AppModel.DEFAULT_RABBITMQ_QUEUE_NAME;
		}
		
		// check rabbitmq host
		if(mapConfig.containsKey("mq_host") && mapConfig.get("mq_host")!=null) {
			try {
				String mq_host = mapConfig.get("mq_host").toString().trim();
				if(!mq_host.isEmpty()) {
					AppModel.RABBITMQ_HOST = mq_host;
				}
			} catch(Exception e) {}
		}
		
		if(mapConfig.containsKey("mq_vhost") && mapConfig.get("mq_vhost")!=null) {
			try {
				String mq_vhost = mapConfig.get("mq_vhost").toString().trim();
				if(!mq_vhost.isEmpty()) {
					AppModel.RABBITMQ_VHOST = mq_vhost;
				}
			} catch(Exception e) {}
		}
		
		if(mapConfig.containsKey("mq_port") && mapConfig.get("mq_port")!=null) {
			try {
				String mq_port = mapConfig.get("mq_port").toString().trim();
				if(!mq_port.isEmpty()) {
					AppModel.RABBITMQ_PORT = mq_port;
				}
			} catch(Exception e) {}
		}
		
		// check rabbitmq user account
		if(mapConfig.containsKey("mq_user") && mapConfig.get("mq_user")!=null) {
			try {
				String mq_user = mapConfig.get("mq_user").toString().trim();
				if(!mq_user.isEmpty()) {
					AppModel.MQ_USER = mq_user;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(mapConfig.containsKey("mq_raw_pass") && mapConfig.get("mq_raw_pass")!=null) {
			try {
				String mq_raw_pass = mapConfig.get("mq_raw_pass").toString().trim();
				if(!mq_raw_pass.isEmpty()) {
					AppModel.MQ_PASS = mq_raw_pass;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(mapConfig.containsKey("mq_encrypted_pass") && mapConfig.get("mq_encrypted_pass")!=null) {
			try {
				String mq_encrypted_pass = mapConfig.get("mq_encrypted_pass").toString().trim();
				if(!mq_encrypted_pass.isEmpty()) {
					String pass = SimpleProtector.decrypt(mq_encrypted_pass);
					AppModel.MQ_PASS = pass;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(AppModel.RABBITMQ_HOST.isEmpty()) {
			AppModel.RABBITMQ_HOST = AppModel.DEFAULT_RABBITMQ_HOST;
		}
    }
    
    private static void _createDefaultConfigFile() {
    	// create default file
		Map<String, Object> mapDefault = new HashMap<String, Object>();
		mapDefault.put("disable_couch_feed", "false");
		mapDefault.put("couch_host", AppModel.DEFAULT_SERVER_COUCH_HOST);
		mapDefault.put("couch_port", AppModel.DEFAULT_SERVER_COUCH_PORT);
		mapDefault.put("couch_db", AppModel.DEFAULT_SERVER_COUCH_DB);
		mapDefault.put("mq_host", AppModel.DEFAULT_RABBITMQ_HOST);
		mapDefault.put("mq_queue_name", AppModel.DEFAULT_RABBITMQ_QUEUE_NAME);
		AppUtilities.writeMapIntoFile(AppModel.CONFIG_FILENAME, mapDefault);
    }
    
	private static boolean getMonitoredVMs(int processPid) {
		MonitoredHost host;
		@SuppressWarnings("rawtypes")
		Set vms;
		try {
			host = MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
			vms = host.activeVms();
		} catch (java.net.URISyntaxException sx) {
			throw new InternalError(sx.getMessage());
		} catch (MonitorException mx) {
			throw new InternalError(mx.getMessage());
		}
		MonitoredVm mvm = null;
		String processName = null;
		try {
			mvm = host.getMonitoredVm(new VmIdentifier(String.valueOf(processPid)));
			processName = MonitoredVmUtil.commandLine(mvm);
			processName = processName.substring(processName.lastIndexOf("\\") + 1, processName.length());
			mvm.detach();
		} catch (Exception ex) {

		}
		for (Object vmid : vms) {
			if (vmid instanceof Integer) {
				int pid = ((Integer) vmid).intValue();
				String name = vmid.toString(); // default to pid if name not available
				try {
					mvm = host.getMonitoredVm(new VmIdentifier(name));
					name = MonitoredVmUtil.commandLine(mvm);
					name = name.substring(name.lastIndexOf("\\") + 1, name.length());
					mvm.detach();
					if ((name.equalsIgnoreCase(processName)) && (processPid != pid))
						return false;
				} catch (Exception x) {
				}
			}
		}
		return true;
	}
}
