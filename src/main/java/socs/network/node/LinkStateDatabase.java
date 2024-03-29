package socs.network.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Vector;

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

	private RouterDescription rd;

	public LinkStateDatabase(RouterDescription routerDescription) {
		rd = routerDescription;
		initLinkStateDatabase();
	}

	/**
	 * Initialize the LSD by adding an entry to the store for this router
	 */
	private void initLinkStateDatabase() {
		LSA lsa = new LSA(rd.getSimulatedIPAddress(), 0);
		// create a link description for this router to itself
		LinkDescription ld = new LinkDescription(rd.getSimulatedIPAddress(), -1, 0);
		lsa.addLink(ld);
		_store.put(lsa.getOriginIp(), lsa);
	}

	/**
	 * Updates this LDS with the contents of the lsaArray. If there are no new
	 * contents, then this returns false. Else it returns true.
	 * 
	 * @param lsaArray
	 * @return
	 */
	public boolean update(Vector<LSA> lsaArray) {

		boolean didUpdate = false;

		// update this LSD with any LSAs in the array that we don't already have
		for (LSA lsa : lsaArray) {
			// if the HashMap already contains an LSA with same originating
			// router
			if (this._store.containsKey(lsa.getOriginIp())) {
				// check if the lsaSeqNumber of the received LSA is greater than
				// the current. if it is, replace the old LSA
				if (this._store.get(lsa.getOriginIp()).getLsaSeqNumber() < lsa.getLsaSeqNumber()) {
					this._store.replace(lsa.getOriginIp(), lsa);
					didUpdate = true;
				}
			} else { // otherwise add the new LSA
				this._store.put(lsa.getOriginIp(), lsa);
				didUpdate = true;
			}
		}

		return didUpdate;
	}

	////////////////////////////////////////////////////////////////////////////
	// Some inner helper classes for running Dijkstra's algorithm
	////////////////////////////////////////////////////////////////////////////

	private class Edge {
		private final String ip1, ip2;
		private int weight;

		public Edge(String ip1, String ip2, int weight) {
			this.ip1 = ip1;
			this.ip2 = ip2;
			this.weight = weight;
		}

		public String getIp1() {
			return this.ip1;
		}

		public String getIp2() {
			return this.ip2;
		}

		public int getWeight() {
			return this.weight;
		}

	}

	private class PathDescription implements Comparable<PathDescription> {

		private final String destinationIp;
		private int distance;
		private LinkedList<Edge> path;

		public PathDescription(String destinationIp, int distance, LinkedList<Edge> path) {
			this.destinationIp = destinationIp;
			this.distance = distance;
			this.path = path;
		}

		public String stringifyPath() {
			StringBuffer buff = new StringBuffer();
			
			if (this.path.isEmpty()) {
			    buff.append(this.destinationIp);
			    buff.append(" -> (0) ");
			    buff.append(this.destinationIp);
			} else {
				for (Edge e : this.path) {
					buff.append(e.getIp1());
					buff.append(" -> ");
					buff.append("(" + e.getWeight() + ") ");
				}
				buff.append(this.destinationIp);
			}
			
			return buff.toString();
		}

		public String getDestinationIp() {
			return this.destinationIp;
		}

		public int getDistance() {
			return this.distance;
		}

		public LinkedList<Edge> getPath() {
			return this.path;
		}

		@Override
		public int compareTo(PathDescription o) {
			if (this.distance < o.distance) {
				return -1;
			} else if (this.distance == o.distance) {
				return 0;
			}
			return 1;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Dijkstra's algorithm
	////////////////////////////////////////////////////////////////////////////

	/**
	 * output the shortest path from this router to the destination with the
	 * given IP address
	 */
	public String getShortestPath(String destinationIP) {
		// Let's build up something of the following form
		// <String destinationIp, int distance, LinkedList<Edge> path>

		ArrayList<PathDescription> confirmed = new ArrayList<PathDescription>();
		PriorityQueue<PathDescription> tentative = new PriorityQueue<PathDescription>();

		// start at the current node
		PathDescription start = new PathDescription(this.rd.getSimulatedIPAddress(), 0, new LinkedList<Edge>());

		// add start to tentative (due to loop design, even though we know it
		// will be immediately added to start)
		tentative.add(start);

		// now, we loop until tentative is empty
		PathDescription curr;
		LSA lsa;
		while (!tentative.isEmpty()) {
			// dequeue from tentative
			curr = tentative.remove();

			// add curr to confirmed
			confirmed.add(curr);

			// get all the neighbors of curr (we can get them by accessing curr's LSA)
			lsa = this._store.get(curr.getDestinationIp());
			
			if (lsa == null) {
				return "No path found. The router you are looking for may not have yet been started.";
			}

			// for each neighbor...
			PathDescription tPath;
			String neighborIP;
			Edge e;
			for (LinkDescription ld : lsa.getLinks()) {
				// ignore link descriptions for curr back to itself
				if (ld.getPortNum() != -1) {
					neighborIP = ld.getDestinationIp();
					// check that the neighbor is not already confirmed (if it
					// is, then we've already found the shortest path to it)
					if (!this.isConfirmed(confirmed, neighborIP)) {
						tPath = this.getTentativePath(tentative, neighborIP);
						if (tPath == null) {
							// create a new Edge
							e = new Edge(curr.getDestinationIp(), ld.getDestinationIp(), ld.getDistance());
							// add a tentative PathDescription for the neighbor
							tentative.add(new PathDescription(neighborIP, curr.getDistance() + ld.getDistance(),
									this.append(curr.getPath(), e)));
						} else {
							// see if we've found a cheaper path to the neighbor, and if 
							// so update the distance and path in the tentative PathDescription
							if (tPath.getDistance() > (curr.getDistance() + ld.getDistance())) {
								e = new Edge(curr.getDestinationIp(), ld.getDestinationIp(), ld.getDistance());
								LinkedList<Edge> newPath = this.append(curr.getPath(), e);
								// remove the old tentative path
								tentative.remove(tPath);
								// add a new, updated one
								tentative.add(new PathDescription(neighborIP, curr.getDistance() + ld.getDistance(),
										newPath));
							}
						}
					}
				}
			}
		}

		// return the path to destinationIP as a string
		for (PathDescription pd : confirmed) {
			if (pd.getDestinationIp().equals(destinationIP)) {
				return pd.stringifyPath();
			}
		}
		return "No path found";
	}

	// helpers for Dijkstra's

	private boolean isConfirmed(ArrayList<PathDescription> confirmed, String ip) {
		for (PathDescription pd : confirmed) {
			if (pd.getDestinationIp().equals(ip)) {
				return true;
			}
		}
		return false;
	}

	private PathDescription getTentativePath(PriorityQueue<PathDescription> tentative, String ip) {
		for (PathDescription pd : tentative) {
			if (pd.getDestinationIp().equals(ip)) {
				return pd;
			}
		}
		return null;
	}

	private LinkedList<Edge> append(LinkedList<Edge> edges, Edge e) {
		LinkedList<Edge> newEdges = (LinkedList<Edge>) edges.clone();
		newEdges.add(e);
		return newEdges;
	}

	// public methods

	/**
	 * Returns true if this LSD contains more LSA entries that in the lsaArray
	 */
	public boolean containsMore(Vector<LSA> lsaArray) {
		HashMap<String, LSA> other = mapify(lsaArray);
		for (String originIp : this._store.keySet()) {
			if (!other.containsKey(originIp)) {
				return true;
			}
		}
		return false;
	}

	private HashMap<String, LSA> mapify(Vector<LSA> lsaArray) {
		HashMap<String, LSA> map = new HashMap<String, LSA>();
		for (LSA l : lsaArray) {
			map.put(l.getOriginIp(), l);
		}
		return map;
	}

	// Getters and setters

	public HashMap<String, LSA> get_Store() {
		return _store;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LSA lsa : _store.values()) {
			sb.append(lsa.getOriginIp()).append("(" + lsa.getLsaSeqNumber() + ")").append(":\t");
			for (LinkDescription ld : lsa.getLinks()) {
				sb.append(ld.getDestinationIp()).append(",").append(ld.getPortNum()).append(",")
						.append(ld.getDistance()).append("\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
