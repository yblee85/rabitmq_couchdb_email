package com.yblee.mqcouch.control;

import java.io.IOException;

import javax.swing.SwingWorker;

import org.json.JSONException;
import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.yblee.mqcouch.model.AppModel;

public class RabbitMQConsumerControl {
	private AppModel model;
	private AppControl control;
	
	public RabbitMQConsumerControl(AppModel model, AppControl control) {
		this.model = model;
		this.control = control;
	}
	
	public void startRabbitMQConsumer() {
		RabbitMQConsumerWorker mqConsumer = new RabbitMQConsumerWorker(model, control);
		mqConsumer.execute();
		System.out.println("RabbitMQ Consumer worker executed");
	}
	
	private class RabbitMQConsumerWorker extends SwingWorker<Void, Boolean> {
		private AppModel model;
		private AppControl control;
		private Channel channel;
		
		public RabbitMQConsumerWorker(AppModel model, AppControl control) {
			this.model = model;
			this.control = control;
			
			channel = initRabbitMQCannel();
			if(channel == null) {
				System.out.println("rabbit mq channel create fail");
				control.shutdownCouchDBConnection();
				System.exit(0);
			}
			try {
				channel.queueDeclare(AppModel.RABBITMQ_QUEUE_NAME, false, false, false, null);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("rabbit mq channel create fail");
				control.shutdownCouchDBConnection();
				System.exit(0);
			}
		}
		
		private Channel initRabbitMQCannel() {
			ConnectionFactory factory = control.initRabbitMQFactory(AppModel.RABBITMQ_HOST, AppModel.RABBITMQ_PORT, AppModel.RABBITMQ_VHOST, AppModel.MQ_USER, AppModel.MQ_PASS);
			try {
				Connection conn = factory.newConnection();
				Channel chan = conn.createChannel();
				return chan;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(AppModel.RABBITMQ_QUEUE_NAME, true, consumer);
			
			try {
				while(true) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					String message = new String(delivery.getBody());
					      
					JSONObject jobj;
					try {
						jobj = new JSONObject(message);
						
						String id = jobj.getString("_id");
						String rev = jobj.getString("_rev");
						String from = jobj.has("from")?jobj.getString("from"):"";
						String to = jobj.getString("to");
						String title = jobj.getString("title");
						String contents = jobj.getString("contents");
						
						CustomEmailSenderSwingWorker emailSender = new CustomEmailSenderSwingWorker(from, to, title, contents);
						emailSender.setAppControl(control);
						emailSender.setDocIdAndRev(id, rev);
						emailSender.execute();
					} catch (JSONException e) {
						e.printStackTrace();
						System.out.println("JSON transform error : " + message);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void done() {
			try {
				get();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("consumer error?! will shut down...");
			control.shutdownCouchDBConnection();
			System.exit(0);
		}
		
	}
	
}
