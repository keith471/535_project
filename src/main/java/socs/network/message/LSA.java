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

	private String originIp; // simulated IP address of the router where this
								// LSA originated
	private int lsaSeqNumber; // version of the LSA, to be
								// compared with last LSA
								// version received by the
								// router from the sender
								// (linkStateId)

	// the links from the origin router to all other routers
	private LinkedList<LinkDescription> links;
	
	public LSA(String originIp, int lsaSeqNumber) {
		this.originIp = originIp;
		this.lsaSeqNumber = lsaSeqNumber;
		this.links = new LinkedList<LinkDescription>();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(originIp + ":").append(lsaSeqNumber + "\n");
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
	public String getOriginIp() {
		return originIp;
	}

	public void setOriginIp(String originIp) {
		this.originIp = originIp;
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
