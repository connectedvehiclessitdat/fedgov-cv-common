package gov.usdot.cv.common.dialog;

import gov.usdot.cv.common.inet.InetPacketException;
import gov.usdot.cv.common.inet.InetPacketSender;
import gov.usdot.cv.common.inet.InetPoint;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

public class DataBundleSender {
	private String forwarderHost;
	private int forwarderPort;
	
	private boolean forwardAll = false;
	private boolean forwarderConfigured = false;
	
	private InetPacketSender forwarder;
	private InetPacketSender sender;
	
	private DataBundleSender(
		String forwarderHost,
		int forwarderPort,
		boolean forwardAll) throws UnknownHostException {
		this.forwarderHost = forwarderHost;
		this.forwarderPort = forwarderPort;
		this.forwardAll = forwardAll;
		
		if (! StringUtils.isEmpty(this.forwarderHost) &&
			! this.forwarderHost.equalsIgnoreCase("localhost") &&
			! this.forwarderHost.equals("127.0.0.1") && 
			(this.forwarderPort >= 0 && this.forwarderPort <= 65535)) {
			forwarderConfigured = true;
		}
	}
	
	public String getForwarderHost() { return this.forwarderHost; }
	public int getForwaderPort() { return this.forwarderPort; }
	
	/**
	 * Sends the data bundle to the target host and port.
	 * 
	 * Case 1: If forwarder is configured and forward all set to true,
	 * all packet will be sent to the forwarder to be routed.
	 * 
	 * Case 2: If the forwarder is configured and forward all set to false,
	 * all ipv6 packet will be sent to the forwarder to be routed.
	 * 
	 * Case 3: If the forwarder is configured and forward all set to false,
	 * all ipv4 packet will be sent directly.
	 * 
	 * Case 4: If the forwarder is not configured, all ipv6 and ipv4 packets
	 * will be sent directly.
	 */
	public void send(
			String targetHost, 
			int targetPort, 
			byte [] payload) throws InetPacketException, UnknownHostException {
		InetPoint client = new InetPoint(getAddressBytes(targetHost), targetPort);
		if (forwarderConfigured) {
			getForwarder(this.forwarderHost, this.forwarderPort).forward(client, payload);
		} else {
			getSender().send(client, payload);
		}
	}
	
	private byte [] getAddressBytes(String host) throws UnknownHostException {
		return InetAddress.getByName( host ).getAddress();
	}
	
	private InetPacketSender getForwarder(String forwarderHost, int forwarderPort) throws UnknownHostException {
		if (this.forwarder != null) return this.forwarder;
		this.forwarder = new InetPacketSender(new InetPoint(getAddressBytes(forwarderHost), forwarderPort));
		this.forwarder.setForwardAll(this.forwardAll);
		return this.forwarder;
	}
	
	private InetPacketSender getSender() {
		if (this.sender != null) return this.sender;
		this.sender = new InetPacketSender();
		return this.sender;
	}
	
	public static class Builder {
		private String forwarderHost;
		private int forwarderPort = -1;
		private boolean forwardAll = false;
		
		public Builder setForwarderHost(String forwarderHost) {
			this.forwarderHost = forwarderHost;
			return this;
		}
		
		public Builder setForwarderPort(int forwarderPort) {
			this.forwarderPort = forwarderPort;
			return this;
		}
		
		public Builder setForwardAll(boolean forwardAll) {
			this.forwardAll = forwardAll;
			return this;
		}
		
		public DataBundleSender build() throws UnknownHostException {
			return new DataBundleSender(
				this.forwarderHost, 
				this.forwarderPort, 
				this.forwardAll);
		}
	}
}