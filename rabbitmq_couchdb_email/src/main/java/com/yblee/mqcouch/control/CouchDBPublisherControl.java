package com.yblee.mqcouch.control;

import javax.swing.SwingWorker;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.impl.StdCouchDbConnector;
import org.json.JSONException;
import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yblee.mqcouch.model.AppModel;

public class CouchDBPublisherControl {
	private AppModel model;
	private AppControl control;
	private CouchDBListnerWorker listenerWorker;
	
	public CouchDBPublisherControl(AppModel model, AppControl control) {
		this.model = model;
		this.control = control;
	}
	
	public void setEnabledPublisher(boolean enabled) {
		if(enabled) {
			listenerWorker = startCouchDBListnerWorker();
		} else {
			ChangesFeed feed = listenerWorker.getFeed();
			if(feed != null && feed.isAlive()) {
				feed.cancel();
			}
		}
	} 
	
	private CouchDBListnerWorker startCouchDBListnerWorker() {
		listenerWorker = new CouchDBListnerWorker(model, control);
		listenerWorker.execute();
		System.out.println("CouchDB listner and publisher worker excuted");
		return listenerWorker;
	}
	
	
	private class CouchDBListnerWorker extends SwingWorker<Void, Boolean> {
		private AppModel model;
		private AppControl control;
		private CouchDbInstance dbInstance;
		private ChangesFeed feed;
		private ConnectionFactory producerFactory;
		
		public CouchDBListnerWorker(AppModel model, AppControl control) {
			this.model = model;
			this.control = control;
			dbInstance = control.getDbInstance();
		}
		
		public ChangesFeed getFeed() {
			return feed;
		}

		private void initCouchDBPublisher() {
			
			CouchDbConnector dbConn = new StdCouchDbConnector(AppModel.SERVER_COUCH_DB, dbInstance);
			ChangesCommand changesCmd = null;
			if(model.getSequence() == 0) {
				changesCmd = new ChangesCommand.Builder().includeDocs(true).build();
			} else {
				changesCmd = new ChangesCommand.Builder().includeDocs(true).since(model.getSequence()).build();
			}
			
			feed = dbConn.changesFeed(changesCmd);
			while(feed.isAlive()) {
				try {
					DocumentChange docChange = feed.next();
					model.setSequence(docChange.getSequence());
					if(!docChange.isDeleted()) {
						String strDoc = docChange.getDoc();
						System.out.println("doc stringified : " + strDoc);
						
						PublishWorker publisher = new PublishWorker(producerFactory, strDoc);
						publisher.execute();
						System.out.println("publish worker executed");
					}
				} catch(InterruptedException e) {
					e.printStackTrace();
					feed.cancel();
					
					// try to get new feed
					feed = null;
					feed = dbConn.changesFeed(changesCmd);
				}
			}
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			producerFactory = control.initRabbitMQFactory(AppModel.RABBITMQ_HOST, AppModel.RABBITMQ_PORT, AppModel.RABBITMQ_VHOST, AppModel.MQ_USER, AppModel.MQ_PASS);
			initCouchDBPublisher();
			return null;
		}
		
		@Override
		protected void done() {
			try {
				get();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("change feed error?! will shut down...");
			if(model.getSequence() != 0) {
				control.updateCouchDBSequence(model.getSequence());
			}
			control.shutdownCouchDBConnection();
			System.exit(0);
		}
	}
	
	private class PublishWorker extends SwingWorker<Void, Boolean> {
		private String data;
		private ConnectionFactory producerFactory;
		
		public PublishWorker(ConnectionFactory producerFactory, String strDoc) {
			this.producerFactory = producerFactory;
			data = strDoc;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			
			if(data == null || data.isEmpty()) {
				return null;
			}
			
			// check if this is valid json format
			// and update time to local machine time 
			JSONObject jobj;
			String newData = "";
			try {
				jobj = new JSONObject(data);
				
				newData = jobj.toString();
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			
			// TODO : producer
		    Connection connection = producerFactory.newConnection();
		    Channel channel = connection.createChannel();
		    
		    channel.queueDeclare(AppModel.RABBITMQ_QUEUE_NAME, false, false, false, null);
		    String message = newData;
		    channel.basicPublish("", AppModel.RABBITMQ_QUEUE_NAME, null, message.getBytes());
		    
		    channel.close();
		    connection.close();
		    
			return null;
		}
		
		@Override
		protected void done() {
			try {
				get();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
