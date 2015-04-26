package com.yblee.mqcouch.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import com.yblee.mqcouch.model.AppModel;

public class AppUtilities {
	public static String FILE_SEPARATOR = System.getProperty("file.separator");
	public static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private static final String IP_PATTERN = 
	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
//	public static String initServerIPAddress() {
//    	String ip_from_url = "";
//    	String ip_result = "";
//    	
//    	// ask server couchdb url
//    	URL qrurl;
//		URLConnection qrc;
//		try {
//			qrurl = new URL("http://" + AppModel.serverCouchDBURL);
//			qrc = qrurl.openConnection();
//			qrc.setReadTimeout(10000); // 10 sec
//			BufferedReader in = new BufferedReader(new InputStreamReader(qrc.getInputStream()));
//	        String inputLine;
//	        String content = "";
//	        while ((inputLine = in.readLine()) != null) { 
//	            content += inputLine;
//	        }
//	        in.close();
//
//	        /** FIX NO INTERNET **/
//	        if(AppUtilities.validate_ip(content)) {
//	        	ip_from_url = content;
//	        }
//		} catch (MalformedURLException e3) {
//			e3.printStackTrace();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//    	// check ip
//    	boolean is_exist_ip_from_url = ip_from_url!=null && !ip_from_url.equals("");
//    	
//		if(is_exist_ip_from_url) {
//			System.out.println("server ip retrieved from url");
//			ip_result = ip_from_url;
//		} else {
//			System.out.println("server ip retrieved from default");
//			ip_result = AppModel.defaultCouchDBHost;
//		}
//	    	
//		
//		// initialize POSModel server ip
//		AppModel.serverCouchDBHost = ip_result;
//		AppModel.serverCouchDBAddress = "http://" + AppModel.serverCouchDBHost + ":"+ AppModel.serverCouchDBPort;
//		
//		//
//    	return ip_result;
//    }
	
	public static boolean validate_ip(final String ip){          
	      Pattern pattern = Pattern.compile(IP_PATTERN);
	      Matcher matcher = pattern.matcher(ip);
	      return matcher.matches();             
	}
	
	public static boolean isCouchDBServerReachable() {
    	try {
            //make a URL to a known source
            URL url = new URL(AppModel.SERVER_COUCH_ADDRESS);

            //open a connection to that source
            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
            urlConnect.setConnectTimeout(5000);

            //trying to retrieve data from the source. If there
            //is no connection, this line will fail
            Object objData = urlConnect.getContent();
            
            /** FIX FROZEN ISSUE 4 **/ // not exactly related but it's good to have
            urlConnect.disconnect();
            
            return true;
	    } catch (UnknownHostException e) {
//	            e.printStackTrace();
	            return false;
	    }
	    catch (IOException e) {
//	            e.printStackTrace();
	            return false;
	    } catch (Exception e) {
//	        	e.printStackTrace();
	        	return false;
	    }
    }
	
//	public static boolean checkAndUpdate() {
//		String serveraddress = "http://" + AppModel.serverCouchDBHost;
//		String servercouchdbport = AppModel.serverCouchDBPort;
//		boolean isUpdateAvailable = false;
//		HttpClient httpServerUpdates = null;
//		
//		try {
//			httpServerUpdates = new StdHttpClient.Builder().connectionTimeout(1800000).socketTimeout(1800000).url(serveraddress + ":" + servercouchdbport).build();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (DbAccessException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		CouchDbInstance dbInstanceServerUpdate = new StdCouchDbInstance(httpServerUpdates);
//		
//		CouchDbConnector dbServerUpdates = new StdCouchDbConnector("install_files_rt7", dbInstanceServerUpdate);
//		
//		try {
//			isUpdateAvailable = AppUtilities.checkUpdates(dbServerUpdates);
//		} catch(ConnectTimeoutException e) {
//			isUpdateAvailable = false;
//		} catch(DbAccessException e) {
//			isUpdateAvailable = false;
//		}
//		
//		if(isUpdateAvailable) {
//			SwingUtilities.invokeLater(new UpdateTask(dbServerUpdates, true));
//		}
//		
//		return isUpdateAvailable;
//	}
	
//	public static boolean checkUpdates (CouchDbConnector dbServer) throws ConnectTimeoutException {
//		boolean updates = false;
//		File file = new File(AppModel.rootDirName + AppUtilities.FILE_SEPARATOR + "PosEmail.jar");
//		long size = 0;
//		ViewQuery view = new ViewQuery().designDocId("_design/pos").viewName("get_pos_email_jar_size").key("5fc84fb55b280fa08e1d956ffc88fbd2");
//		ViewResult result = dbServer.queryView(view);
//		for(Row row : result.getRows()) {
//			JsonNode param = row.getValueAsNode();
//			size = param.getLongValue();
//		}
//		if(size != 0 && file.length() != size) {
//			System.out.println("update available");
//			updates = true;
//		}
//		return updates;
//	}
	
	public static boolean writeMapIntoFile(String path, Map<String, Object> map) {
		File f = new File(path);
		
		try {
			if(map != null && !map.isEmpty()) {
				BufferedWriter out = new BufferedWriter(new FileWriter(f));
				for(String strKey : map.keySet()) {
					String val = map.get(strKey).toString().trim();
					out.write("[" + strKey.trim() + "]" + LINE_SEPARATOR);
					out.write(val + LINE_SEPARATOR);
				}
				out.close();
				return true;
			} else {
				return true;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Map readFileIntoMap(String path) {
		BufferedReader reader;

		Map<String, Object> mapFile = new TreeMap<String, Object>();
		
		try {
			reader = new BufferedReader( new FileReader (path));
			String line = null;
			String currentKey = "";
			String strContents = "";
			while( ( line = reader.readLine() ) != null ) {
				line = line.trim();
				
//				if(line.isEmpty() && currentKey != null && currentKey != "content")
//					continue;
				
				if(line.startsWith("[") && line.endsWith("]")) {
					
					// transform content and put with currentKey
					if(!currentKey.isEmpty() && !strContents.isEmpty()) {
						strContents = strContents.trim();
						mapFile.put(currentKey, strContents);
					}
					
					// after transform content and put into a map, clean content for next part
					strContents = "";
					currentKey = line.substring(1, line.length()-1);
				} else {
					strContents = strContents.concat(line + LINE_SEPARATOR);
				}
			}
			
			// last one transform content and put with currentKey
			if(!currentKey.isEmpty() && !strContents.isEmpty()) {
				strContents = strContents.trim();
				mapFile.put(currentKey, strContents);
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			mapFile = null;
		} catch (IOException e) {
			e.printStackTrace();
			mapFile = null;
		} catch(Exception e) {
			e.printStackTrace();
			mapFile = null;
		}
		
		return mapFile;
	}
	
	// this will go through dir and return files path
	public static List<String> dirCrawl(String rootDirPath) {
		List<String> listFilePath = new ArrayList<String>();
		File curDir = new File(rootDirPath);
		File[] files = curDir.listFiles();
		for(File f : files) {
			listFilePath.add(f.getAbsolutePath());
		}
		return listFilePath;
	}
	
	public static void addLibraryPath(String pathToAdd) {
		Field usrPathsField;
		try {
			usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
			usrPathsField.setAccessible(true);
			 
		    //get array of paths
		    String[] paths;
			paths = (String[])usrPathsField.get(null);
		 
		    //check if the path to add is already present
		    for(String path : paths) {
		        if(path.equals(pathToAdd)) {
		            return;
		        }
		    }
		 
		    //add the new path
		    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		    newPaths[newPaths.length-1] = pathToAdd;
		    usrPathsField.set(null, newPaths);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
