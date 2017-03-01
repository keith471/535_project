package socs.network.message;

import java.io.Serializable;

/**
 * Describes a link
 * 
 * @author kstricks
 *
 */
public class LinkDescription implements Serializable {

	private String destinationIp; // the simulated IP address of the destination
									// node
	private int portNum; // the port that can be used from the LSA origin node
							// to reach the node with
							// address destinationIp (-1 if the destination node
							// is the
							// origin)
	private int distance; // the distance to the node (0 if the destination node
							// is the origin)

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

	public int getPortNum() {
		return portNum;
	}

	public int getDistance() {
		return distance;
	}
}
