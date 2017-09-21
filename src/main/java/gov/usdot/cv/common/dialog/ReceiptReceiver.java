package gov.usdot.cv.common.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import gov.usdot.cv.common.dialog.ReceiptReceiverException;
import gov.usdot.cv.common.util.PropertyLocator;

public abstract class ReceiptReceiver implements MessageListener {
	
	private static final Logger log = Logger.getLogger(ReceiptReceiver.class);
	
	private final String receiptJmsHost;
	private final int receiptJmsPort;
	private final String topicName;
		
	private Connection connection = null;
	private Session session = null;
	private MessageConsumer consumer = null;
	
	public ReceiptReceiver(String topicName) {
		this(null, 0, topicName);
	}
	
	public ReceiptReceiver(String receiptJmsHost, int receiptJmsPort, String topicName) {
		this.receiptJmsHost = receiptJmsHost;
		this.receiptJmsPort = receiptJmsPort;
		this.topicName = topicName;
	}

	public void initialize() throws ReceiptReceiverException {
			
		log.info("Constructing receipt Receiver ...");
		String brokerUrl = null;
		if (! StringUtils.isEmpty(this.receiptJmsHost) && this.receiptJmsPort != -1) {
			StringBuilder sb = new StringBuilder();
			sb.append("nio://").append(this.receiptJmsHost).append(':').append(this.receiptJmsPort);
			brokerUrl = sb.toString();
		} else {
			brokerUrl = PropertyLocator.getString("messaging.external.connection.url");
		}
		
		if (brokerUrl == null) {
			throw new ReceiptReceiverException("Missing property 'messaging.external.connection.url'.");
		}
	
		String username = PropertyLocator.getString("messaging.external.connection.user");
		if (username == null) {
			throw new ReceiptReceiverException("Missing property 'messaging.external.connection.user'.");
		}
	
		String password = PropertyLocator.getString("messaging.external.connection.password");
		if (password == null) {
			throw new ReceiptReceiverException("Missing property 'messaging.external.connection.password'.");
		}
		
		ConnectionFactory factory = new ActiveMQConnectionFactory(username, password, brokerUrl);
		try {
			startWorker();
			this.connection = factory.createConnection();
			this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			this.consumer = session.createConsumer(session.createTopic(this.topicName));
			this.consumer.setMessageListener(this);
			this.connection.start();
		} catch (JMSException jmse) {
			stopWorker();
			throw new ReceiptReceiverException("Failed to initialize ReceiptReceiver.", jmse);
		}
	}
	
	public void dispose() throws ReceiptReceiverException {
		try {
			stopWorker();
			if ( consumer != null ) {
				consumer.close();
				consumer = null;
			}
			if ( session != null ) {
				session.close();
				session = null;
			}
			if ( connection != null ) {
				connection.stop();
				connection.close();
				connection = null;
			}
		} catch (JMSException jmse) {
			throw new ReceiptReceiverException("Failed to dispose ReceiptReceiver.", jmse);
		}
	}

	@Override
	public void onMessage(Message msg) {
		if (msg != null && msg instanceof TextMessage) {
			try {
				String record = ((TextMessage) msg).getText();
				Receipt receipt = new Receipt(record);
				receipts.add(receipt);
				log.debug(String.format("Received Receipt: '%s'", receipt));
				wakeUpWorker();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	//
	// Worker region
	//
	
	protected List<Receipt> receipts = Collections.synchronizedList(new ArrayList<Receipt>());
	
	private static final Integer work = new Integer(0);
	private Boolean workerRun = null;

	private synchronized void startWorker() {
		log.debug("Started Receipt Receiver Worker");
		if ( workerRun == null ) {
			workerRun = true;
			new Thread(new Runnable() {
			     public void run()
			     {
			          runWorker();
			     }
			}).start(); 
		}
	}
	
	private synchronized void stopWorker() {
		if ( workerRun != null && workerRun == true ) {
			workerRun = false;
			wakeUpWorker();
		}
	}
	
	private void runWorker() {
		while( workerRun != null && workerRun == true ) {
			processReceipts();
			synchronized(work) {
				try {
					work.wait(2000);
				} catch (InterruptedException unused) {
				}
			}
		}
	}
	
	public static void wakeUpWorker() {
		synchronized (work) {
			work.notify();
		}
	}

	abstract protected void processReceipts(); 

}
