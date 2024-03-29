package socs.network.node;

/**
 * Both routers should add this object to their ports array
 * 
 * @author kstricks
 *
 */
public class Link {

	// the routers on either end of the link
	private RouterDescription router1; // the router that owns this Link object
	private RouterDescription router2; // the remote router
	private int weight; // the weight of the link (distance)

	public Link(RouterDescription r1, RouterDescription r2, int weight) {
		this.router1 = r1;
		this.router2 = r2;
		this.weight = weight;
	}
	
	// Setters and Getters
	public RouterDescription getRouter1() {
		return router1;
	}

	public RouterDescription getRouter2() {
		return router2;
	}
	
	public int getWeight() {
		return weight;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((router1 == null) ? 0 : router1.hashCode());
		result = prime * result + ((router2 == null) ? 0 : router2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Link other = (Link) obj;
		if (router1 == null) {
			if (other.router1 != null) {
				return false;
			}
		} else if (!router1.equals(other.router1)) {
			return false;
		}
		if (router2 == null) {
			if (other.router2 != null) {
				return false;
			}
		} else if (!router2.equals(other.router2)) {
			return false;
		}
		return true;
	}

}
