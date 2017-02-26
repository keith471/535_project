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
	private String srcIP; // the original source of the packet
	private String dstIP; // the eventual destination of the packet

	// common header
	private MessageType messageType;
	private String routerID;

	// the node immediately prior to the receiving node in the chain
	private String neighborID;

	// used by LSAUPDATE
	private Vector<LSA> lsaArray = null;

	private String errorMsg;
	
	// for ADDLINK
	private int weight;

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
		this.neighborID = srcIP;
	}

	public String getSrcProcessIP() {
		return srcProcessIP;
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

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
