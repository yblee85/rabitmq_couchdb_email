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
