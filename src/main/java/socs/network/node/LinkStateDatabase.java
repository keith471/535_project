package socs.network.node;

import java.util.HashMap;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

/**
 * 
 * @author kstricks
 *
 */
public class LinkStateDatabase {

	// map: linkID (in LinkDescription) => LSAInstance
	HashMap<String, LSA> _store = new HashMap<String, LSA>();

	private RouterDescription rd = null;

	public LinkStateDatabase(RouterDescription routerDescription) {
		rd = routerDescription;
		LSA l = initLinkStateDatabase();
		_store.put(l.linkStateID, l);
	}

	/**
	 * output the shortest path from this router to the destination with the
	 * given IP address
	 */
	String getShortestPath(String destinationIP) {
		// TODO: fill the implementation here
		return null;
	}

	// initialize the link state database by adding an entry about the router
	// itself
	private LSA initLinkStateDatabase() {
		LSA lsa = new LSA();
		lsa.linkStateID = rd.getSimulatedIPAddress();
		lsa.lsaSeqNumber = Integer.MIN_VALUE;
		LinkDescription ld = new LinkDescription();
		ld.linkID = rd.getSimulatedIPAddress();
		ld.portNum = -1;
		ld.tosMetrics = 0;
		lsa.links.add(ld);
		return lsa;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LSA lsa : _store.values()) {
			sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
			for (LinkDescription ld : lsa.links) {
				sb.append(ld.linkID).append(",").append(ld.portNum).append(",").append(ld.tosMetrics).append("\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
