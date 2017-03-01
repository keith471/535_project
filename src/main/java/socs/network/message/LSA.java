package socs.network.message;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Link state advertisement. This contains the link descriptions for the links
 * that the origin node (linkStateID) is sure of. i.e. the link descriptions to
 * itself and to its neighbors
 * 
 * @author kstricks
 *
 */
public class LSA implements Serializable {

	private String linkStateID; // simulated IP address of the router where this
								// LSA originated
	private int lsaSeqNumber; // version of the LSA, to be
								// compared with last LSA
								// version received by the
								// router from the sender
								// (linkStateId)

	//
	private LinkedList<LinkDescription> links = new LinkedList<LinkDescription>();
	
	public LSA(String linkStateID, int lsaSeqNumber) {
		this.linkStateID = linkStateID;
		this.lsaSeqNumber = lsaSeqNumber;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(linkStateID + ":").append(lsaSeqNumber + "\n");
		for (LinkDescription ld : links) {
			sb.append(ld);
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public void incrementLsaSeqNumber() {
		lsaSeqNumber++;
	}

	public void addLink(LinkDescription ld) {
		this.links.add(ld);
	}

	// Setters and Getters
	public String getLinkStateID() {
		return linkStateID;
	}

	public void setLinkStateID(String linkStateID) {
		this.linkStateID = linkStateID;
	}

	public int getLsaSeqNumber() {
		return lsaSeqNumber;
	}

	public void setLsaSeqNumber(int lsaSeqNumber) {
		this.lsaSeqNumber = lsaSeqNumber;
	}

	public LinkedList<LinkDescription> getLinks() {
		return links;
	}

}
