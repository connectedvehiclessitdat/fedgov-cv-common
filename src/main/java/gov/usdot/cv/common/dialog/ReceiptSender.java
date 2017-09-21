package gov.usdot.cv.common.dialog;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.deleidos.rtws.commons.net.jms.BasicMessageProducer;
import com.deleidos.rtws.commons.net.jms.JMSFactory;
import com.deleidos.rtws.commons.net.jms.RoundRobinJMSConnectionFactory;

public class ReceiptSender {
	private static Logger logger = Logger.getLogger(ReceiptSender.class);
	
	private JMSFactory 				factory;
	private BasicMessageProducer 	session;

	public static class Builder {
		private String topicName;
		private String brokerUrl;
		private String username;
		private String password;
		
		public Builder setTopicName(String topicName) {
			this.topicName = topicName;
			return this;
		}
		
		public Builder setBrokerUrl(String brokerUrl) {
			this.brokerUrl = brokerUrl;
			return this;
		}
		
		public Builder setUsername(String username) {
			this.username = username;
			return this;
		}
		
		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}
		
		public ReceiptSender build() {
			if (this.brokerUrl == null) {
				throw new NullPointerException("Receipt sender broker url is null.");
			}
			
			if (this.username == null) {
				throw new NullPointerException("Receipt sender username is null.");
			}

			if (this.password == null) {
				throw new NullPointerException("Receipt sender password is null.");
			}
			
			RoundRobinJMSConnectionFactory cf = new RoundRobinJMSConnectionFactory();
			cf.setBrokerURL(this.brokerUrl);
			cf.setUserName(this.username);
			cf.setPassword(this.password);
			
			ReceiptSender sender = new ReceiptSender();
			sender.factory = new JMSFactory();
			sender.factory.setConnectionFactory(cf);
			sender.session = sender.factory.createSimpleTopicProducer(this.topicName);
			return sender;
		}
	}

	private ReceiptSender() {
		// Prevent direct instantiation of the class
	}

	public void close() {
		if (this.session != null) {
			try { this.session.close(); } catch (Exception ex) {}
		}
	}
	
	public void send(String receiptId) throws JMSException {
		if (receiptId == null) return;
		
		boolean sent = false;
		while (! sent) try {
			connect(this.session);
			this.session.send(buildMessage(receiptId));
			sent = true;
		} catch (JMSException e) {
			this.session.close();
		}
	}
	
	private void connect(BasicMessageProducer session) {
		boolean stuck = false;
		while (! session.isConnected()) try {
			this.session.reset();
		} catch (JMSException e) {
			if (!stuck) {
				logger.warn("Unable to establish connection to external jms server; waiting...", e);
				stuck = true;
			}
			try { Thread.sleep(300); } catch (InterruptedException ignore) { }
		}
	}
	
	private TextMessage buildMessage(String data) throws JMSException {
		TextMessage message = this.session.createTextMessage();
		message.setText(data);
		return message;
	}
}