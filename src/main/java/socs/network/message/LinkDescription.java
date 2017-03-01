package socs.network.message;

import java.io.Serializable;

/**
 * Describes a link
 * 
 * @author kstricks
 *
 */
public class LinkDescription implements Serializable {

	private String destinationIp; // the simulated IP address of the node that
							// originated the LSA (the destination node)
	private int portNum; // the port that can be used to reach the node with
							// address linkID (-1 if the destination node is the
							// current one)
	private int distance; // the distance to the node

	public LinkDescription(String linkID, int portNum, int distance) {
		this.destinationIp = linkID;
		this.portNum = portNum;
		this.distance = distance;
	}

	@Override
	public String toString() {
		return destinationIp + "," + portNum + "," + distance;
	}

	// Getters and Setters
	public String getDestinationIp() {
		return destinationIp;
	}

	public void setDestinationIp(String destinationIp) {
		this.destinationIp = destinationIp;
	}

	public int getPortNum() {
		return portNum;
	}

	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
}
