package socs.network.message;

import java.io.Serializable;
import java.util.Vector;

/**
 * Defines the message format for communication between routers
 * 
 * @author kstricks
 *
 */
public class SOSPFPacket implements Serializable, Cloneable {

	// for inter-process communication
	private String srcProcessIP;
	private int srcProcessPort;

	// simulated IP address
	private String srcIP; // the original source of the packet
	private String dstIP; // the eventual destination of the packet

	// common header
	private MessageType messageType;
	private String routerID;

	// the node immediately prior to the receiving node in the chain (for LSAUPDATE)
	private String precedingNodeIP;

	// used by LSAUPDATE
	private Vector<LSA> lsaArray;

	private String errorMsg;
	
	// for ADDLINK
	private int weight;

	/**
	 * Many overloaded constructors for instantiating packets for various
	 * purposes
	 */

	public SOSPFPacket() {
		this.messageType = MessageType.SUCCESS;
	}

	public SOSPFPacket(String errMsg) {
		this.messageType = MessageType.ERROR;
		this.errorMsg = errMsg;
	}

	public SOSPFPacket(MessageType mt, String srcProcessIP, int srcProcessPort, String srcIP) {
		this.messageType = mt;
		this.srcProcessIP = srcProcessIP;
		this.srcProcessPort = srcProcessPort;
		this.srcIP = srcIP;
		this.precedingNodeIP = srcIP;
	}
	
	public SOSPFPacket(MessageType mt, String srcProcessIP, int srcProcessPort, String srcIP, Vector<LSA> lsaArray) {
		this.messageType = mt;
		this.srcProcessIP = srcProcessIP;
		this.srcProcessPort = srcProcessPort;
		this.srcIP = srcIP;
		this.precedingNodeIP = srcIP;
		this.lsaArray = lsaArray;
	}

	public SOSPFPacket(MessageType mt, String srcProcessIP, int srcProcessPort, String srcIP, int weight) {
		this.messageType = mt;
		this.srcProcessIP = srcProcessIP;
		this.srcProcessPort = srcProcessPort;
		this.srcIP = srcIP;
		this.precedingNodeIP = srcIP;
		this.weight = weight;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	// getters and setters

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

	public String getPrecedingNodeIP() {
		return precedingNodeIP;
	}

	public void setPrecedingNodeIP(String precedingNodeIP) {
		this.precedingNodeIP = precedingNodeIP;
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
}
