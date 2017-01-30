package socs.network.message;

import java.io.Serializable;
import java.util.Vector;

/**
 * Defines the message format for communication between routers
 * 
 * @author kstricks
 *
 */
public class SOSPFPacket implements Serializable {

	// for inter-process communication
	private String srcProcessIP;
	private int srcProcessPort;

	// simulated IP address
	private String srcIP;
	private String dstIP;

	// common header
	private MessageType messageType;
	private String routerID;

	// used by HELLO message to identify the sender of the message
	// e.g. when router A sends HELLO to its neighbor, it has to fill this field
	// with its own
	// simulated IP address
	private String neighborID; // sender's simulated IP address

	// used by LSAUPDATE
	private Vector<LSA> lsaArray = null;

	private String errorMsg;

	/**
	 * Convenience initializer for errors
	 * 
	 * @param errMsg
	 */
	public SOSPFPacket(String errMsg) {
		this.messageType = MessageType.ERROR;
		this.errorMsg = errMsg;
	}

	public SOSPFPacket() {
		this.messageType = MessageType.SUCCESS;
	}

	public SOSPFPacket(MessageType mt, String srcProcessIP, int srcProcessPort, String srcIP) {
		this.messageType = mt;
		this.srcProcessIP = srcProcessIP;
		this.srcProcessPort = srcProcessPort;
		this.srcIP = srcIP;
	}

	public String getSrcProcessIP() {
		return srcProcessIP;
	}

	public void setSrcProcessIP(String srcProcessIP) {
		this.srcProcessIP = srcProcessIP;
	}

	public int getSrcProcessPort() {
		return srcProcessPort;
	}

	public void setSrcProcessPort(int srcProcessPort) {
		this.srcProcessPort = srcProcessPort;
	}

	public String getSrcIP() {
		return srcIP;
	}

	public void setSrcIP(String srcIP) {
		this.srcIP = srcIP;
	}

	public String getDstIP() {
		return dstIP;
	}

	public void setDstIP(String dstIP) {
		this.dstIP = dstIP;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType sospfType) {
		this.messageType = sospfType;
	}

	public String getRouterID() {
		return routerID;
	}

	public void setRouterID(String routerID) {
		this.routerID = routerID;
	}

	public String getNeighborID() {
		return neighborID;
	}

	public void setNeighborID(String neighborID) {
		this.neighborID = neighborID;
	}

	public Vector<LSA> getLsaArray() {
		return lsaArray;
	}

	public void setLsaArray(Vector<LSA> lsaArray) {
		this.lsaArray = lsaArray;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
