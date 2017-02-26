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
	private HashMap<String, LSA> _store = new HashMap<String, LSA>();

	private RouterDescription rd = null;

	public LinkStateDatabase(RouterDescription routerDescription) {
		rd = routerDescription;
		LSA l = initLinkStateDatabase();
		_store.put(l.getLinkStateID(), l);
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
		LSA lsa = new LSA(rd.getSimulatedIPAddress(), Integer.MIN_VALUE);
		LinkDescription ld = new LinkDescription(rd.getSimulatedIPAddress(), -1, 0);
		lsa.getLinks().add(ld);
		return lsa;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LSA lsa : _store.values()) {
			sb.append(lsa.getLinkStateID()).append("(" + lsa.getLsaSeqNumber() + ")").append(":\t");
			for (LinkDescription ld : lsa.getLinks()) {
				sb.append(ld.getLinkID()).append(",").append(ld.getPortNum()).append(",").append(ld.getTosMetrics()).append("\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public HashMap<String, LSA> get_Store() {
		return _store;
	}

}
