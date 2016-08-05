package com.yblee.mqcouch.email;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.yblee.mqcouch.model.AppModel;
 
public class SendMailSSL {
	
	private String authUSer, authPass;
	
	public SendMailSSL(String authUser, String authPass) {
		if(authUser != null && !authUser.isEmpty()) {
			this.authUSer = authUser;
			this.authPass = authPass;
		} 
	} 
	

	public void sendEmailSSLExample() {
		Properties props = new Properties();
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.socketFactory.port", "465");
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.host", "secure.emailsrvr.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
 
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(authUSer, authPass);
				}
			});
		
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("from@no-spam.com"));
//			message.setFrom(InternetAddress.parse("from@no-spam.com")[0]);
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("to1@no-spam.com, to2@no-spam.com"));
			message.setSubject("Testing Subject");
			// html doesn't work
//			message.setText("<html><body><h1>hey, what's up?</h1><br><h2>just checking...</h2></body>></html>");
			message.setText("Dear Mail Crawler," +
					"\n\n No spam to my email, please!");
 
			Transport.send(message);
 
			System.out.println("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean sendEmailSSL(String from, String to, String title, String content) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "secure.emailsrvr.com");
//		props.put("mail.smtp.host", "smtp.emailsrvr.com");
		props.put("mail.smtp.socketFactory.port", "465");
//		props.put("mail.smtp.socketFactory.port", "2525");	//25, 587, 8025, 2525
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.localhost", "myemailserver");
 
		try {
			
			String strFrom = ((from!=null && !from.isEmpty())?from:AppModel.SENDER_EMAIL);
			
			if(strFrom.contains("gmail"))	props.put("mail.smtp.host", "smtp.gmail.com");	// gmail account doesn't seem to work here...
			
			Session session = Session.getDefaultInstance(props,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(authUSer, authPass);
						}
					});
			if(AppModel.isEnabledDebug()) {
				session.setDebug(true);
			}
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(strFrom));
//			System.out.println("from : " + message.getFrom()[0]);
//			message.setFrom(InternetAddress.parse(strFrom)[0]);
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(title);
			// html doesn't work
//			message.setText("<html><body><h1>hey, what's up?</h1><br><h2>just checking...</h2></body>></html>");
			message.setText(content);
 
			Transport.send(message);
 
			System.out.println("Email Sent");
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
//			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
}