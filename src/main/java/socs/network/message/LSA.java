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
								// (originIp)

	// the links from the origin router to its neighbors
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

	public void removeLink(String remoteIp) {
		int i;
		boolean found = false;
		for (i = 0; i < this.links.size(); i++) {
			if (this.links.get(i).getDestinationIp().equals(remoteIp)) {
				found = true;
				break;
			}
		}

		if (found) {
			this.links.remove(i);
		}
	}

	// Setters and Getters
	public String getOriginIp() {
		return originIp;
	}

	public int getLsaSeqNumber() {
		return lsaSeqNumber;
	}

	public LinkedList<LinkDescription> getLinks() {
		return links;
	}

}
