package com.yblee.mqcouch.control;

import java.net.MalformedURLException;
import java.util.Map;

import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import com.rabbitmq.client.ConnectionFactory;
import com.yblee.mqcouch.model.AppModel;
import com.yblee.mqcouch.util.AppUtilities;

public class AppControl {
	private AppModel model;
	private CouchDbInstance dbInstance;
	
	public AppControl(AppModel model) {
		this.model = model;
		
		// initiate couchdb instance if needed
		if(!model.isDisabledCouchFeed()) {
			boolean isConnectionEstablished = initCouchDBInstance(30000);
			if(isConnectionEstablished) {
				CouchDBPublisherControl couchPublisher = new CouchDBPublisherControl(model, AppControl.this);
				couchPublisher.setEnabledPublisher(true);
			} else {
				System.out.println("Fail to create DB instance");
				System.exit(0);
			}
		}
		
		// init rabbitmq consumer
		RabbitMQConsumerControl mqConsumer = new RabbitMQConsumerControl(model, AppControl.this);
		mqConsumer.startRabbitMQConsumer();
	}
	
	public CouchDbInstance getDbInstance() {
		return dbInstance;
	}

	public void updateCouchDBSequence(long newSeq) {
		Map<String, Object> mapConfig = AppUtilities.readFileIntoMap(AppModel.CONFIG_FILENAME);
		mapConfig.put("couch_db_sequence", newSeq + "");
		AppUtilities.writeMapIntoFile(AppModel.CONFIG_FILENAME, mapConfig);
	}
	
	private boolean initCouchDBInstance(int timeout) {
		// init couchdb feed
		HttpClient httpClient;
		try {
			if(AppModel.SERVER_COUCH_USER.isEmpty() || AppModel.SERVER_COUCH_PASS.isEmpty()) {
				System.out.println("=========== httpClient without cridential");
				httpClient = new StdHttpClient.Builder().connectionTimeout(timeout).socketTimeout(timeout).url(AppModel.SERVER_COUCH_ADDRESS).build();
			} else {
				System.out.println("============ httpClient with cridential");
				httpClient = new StdHttpClient.Builder().connectionTimeout(timeout).socketTimeout(timeout).url(AppModel.SERVER_COUCH_ADDRESS).username(AppModel.SERVER_COUCH_USER).password(AppModel.SERVER_COUCH_PASS).build();
			}
			dbInstance = new StdCouchDbInstance(httpClient);
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void shutdownCouchDBConnection() {
		if(dbInstance != null) {
			dbInstance.getConnection().shutdown();
		}
	}
	
	public ConnectionFactory initRabbitMQFactory(String host, String port, String vhost, String user, String pass) {
		// init rabbitmq 
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		
		if(vhost != null && !vhost.isEmpty()) {
			factory.setVirtualHost(vhost);
		}
		
		if(port != null && !port.isEmpty()) {
			int numPort = 0;
			try {
				numPort = Integer.parseInt(port);
				if(numPort>0) {
					factory.setPort(numPort);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(user != null && !user.isEmpty()) {
			factory.setUsername(user);
		}
		
		if(pass != null && !pass.isEmpty()) {
			factory.setPassword(pass);
		}
		
		return factory;
	}
}
