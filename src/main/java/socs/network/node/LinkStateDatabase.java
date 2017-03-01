package socs.network.node;

import java.util.HashMap;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

/**
 * Each router contains one of these. When it needs to find a route to another
 * node in the network, it runs Dijkstra's alg over this
 * 
 * @author kstricks
 *
 */
public class LinkStateDatabase {

	// ip address of node from where the LSA update originated => LSA instance
	private HashMap<String, LSA> _store = new HashMap<String, LSA>();

	private RouterDescription rd = null;

	public LinkStateDatabase(RouterDescription routerDescription) {
		rd = routerDescription;
		initLinkStateDatabase();
	}

	/**
	 * output the shortest path from this router to the destination with the
	 * given IP address
	 */
	String getShortestPath(String destinationIP) {
		// TODO: fill the implementation here
		return null;
	}

	/**
	 * Initialize the LSD by adding an entry to the store for this router
	 * 
	 * @return
	 */
	private void initLinkStateDatabase() {
		LSA lsa = new LSA(rd.getSimulatedIPAddress(), Integer.MIN_VALUE);
		LinkDescription ld = new LinkDescription(rd.getSimulatedIPAddress(), -1, 0);
		lsa.addLink(ld);
		_store.put(lsa.getLinkStateID(), lsa);
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
