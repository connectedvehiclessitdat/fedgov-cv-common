package gov.usdot.cv.common.inet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.log4j.Logger;

/**
 * Sender/Forwarder helper class for use by Forwarder, Transport, and Data Sink that need to send packets around
 */
public class InetPacketSender {
	
	private static final Logger log = Logger.getLogger(InetPacketSender.class);

	/**
	 * Inet address and port to forward packets to
	 */
	private InetPoint frwdPoint;
	
	/**
	 * Specifies whether outbound IPv4 messages should be send directly or forwarded. Default is send directly.
	 * To force forwarding IPv4 messages, set this variable to true.
	 */
	private boolean forwardAll;

	public InetPacketSender() {}
	
	/**
	 * Creates an instance of the forwarder/sender helper class.
	 * @param frwdPoint is the destination to use for forwarding
	 */
	public InetPacketSender(InetPoint frwdPoint) {
		this.frwdPoint = frwdPoint;
	}
	
	/**
	 * Forward packet. Intended client is the forwarder that received a packet
	 * @param inbound UDP packet
	 * @throws InetPacketException 
	 */
	public void forward(DatagramPacket packet) throws InetPacketException {
		if ( packet == null ) {
			log.warn("Ignoring forward request for null packet");
			return;
		}
		if ( frwdPoint == null )
			throw new InetPacketException("Couldn't forward packet. Reason: Forwarding destination is not defined.");
		send(frwdPoint, new InetPacket(packet).getBundle());
	}
	
	/**
	 * Send packet. Intended client is the forwarder that sends outbound packet
	 * @param packet outbound packet that contains destination+payload bundle
	 * @throws InetPacketException 
	 */
	public void send(DatagramPacket packet) throws InetPacketException {
		if ( packet == null ) {
			log.warn("Ignoring send request for null packet");
			return;
		}
		InetPacket p = new InetPacket(packet);
		InetPoint point = p.getPoint();
		if ( point == null )
			throw new InetPacketException("Couldn't send packet. Reason: Destination is not defined in the packet (not a bundle?)");
		send(point, p.getPayload());
	}
	
	/**
	 * Forward payload to be sent to dstPoint. Intended clients are Transport or Data Sink sending via forwarder
	 * @param dstPoint destination address and port for forwarder to forward to
	 * @param payload data to forward
	 * @throws InetPacketException
	 */
	public void forward(InetPoint dstPoint, byte[] payload) throws InetPacketException {
		if ( dstPoint == null || payload == null )
			throw new InetPacketException("Invalid Parameters. Parameters destination point and payload can not be null");
		if ( frwdPoint == null )
			log.warn("Couldn't forward packet. Reason: Forwarding destination is not defined.");
		if ( frwdPoint != null && (dstPoint.isIPv6Address() || isForwardAll()) ) {
			send(frwdPoint, new InetPacket(dstPoint, payload).getBundle());
		} else {
			log.debug("Using direct send instead of forwarding");
			send(dstPoint, payload);
		}
	}
	
	/**
	 * Forward payload to be sent to dstPoint. Intended clients are Transport or Data Sink sending via forwarder or direct
	 * @param dstPoint destination address and port of the final destination
	 * @param payload data to forward or send
	 * @param fromForwarder whether the original request came through a forwarder
	 * @throws InetPacketException
	 */
	public void forward(InetPoint dstPoint, byte[] payload, boolean fromForwarder) throws InetPacketException {
		if ( dstPoint == null || payload == null )
			throw new InetPacketException("Invalid Parameters. Parameters destination point and payload can not be null");
		if ( frwdPoint != null && (dstPoint.isIPv6Address() || isForwardAll() || fromForwarder) ) {
			send(frwdPoint, new InetPacket(dstPoint, payload).getBundle());
		} else {
			log.debug("Using direct send instead of forwarding");
			send(dstPoint, payload);
		}
	}
	
	/**
	 * Send payload to the destination specified. Intended clients are Transport or Data Sink sending directly to the client
	 * @param dstPoint destination address and port to send to
	 * @param payload data to send
	 * @throws InetPacketException
	 */
	public void send(InetPoint dstPoint, byte[] payload) throws InetPacketException {
		if ( dstPoint == null || payload == null )
			throw new InetPacketException("Invalid Parameters. Parameters destination point and payload can not be null");
		DatagramSocket sock = null;
	    try {
	        DatagramPacket packet = new DatagramPacket(payload, payload.length, dstPoint.getInetAddress(), dstPoint.port);
	        sock = new DatagramSocket();
	        sock.send(packet);
	    } catch (SocketException ex) {
	    	throw new InetPacketException("Couldn't send packet because socket closed.", ex);
	    } catch (IOException ex) {
	    	throw new InetPacketException("Couldn't send packet due to IO exception.", ex);
	    } finally {
			if ( sock != null ) {
				if ( !sock.isClosed() )
					sock.close();
				sock = null;
			}
	    }
	}
	
	/**
	 * Reports whether outbound IPv4 messages should be send directly or forwarded. 
	 * @return true if IPv4 packets are forwarded in addition to IPv6 packets
	 */
	public boolean isForwardAll() {
		return forwardAll;
	}

	/**
	 * 
	 * @param forwardAll Directs how to handle IPv4 messages. 
	 * Specify true to force forwarding IPv4 messages, and false to always send them directly.
	 */
	public void setForwardAll(boolean forwardAll) {
		this.forwardAll = forwardAll;
	}
	
}
