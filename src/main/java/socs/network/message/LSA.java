package socs.network.message;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Link state advertisement
 * 
 * @author kstricks
 *
 */
public class LSA implements Serializable {

	public String linkStateID; // simulated IP address of the router where this
								// LSA originated
	public int lsaSeqNumber = Integer.MIN_VALUE; // version of the LSA, to be
													// compared with last LSA
													// version received by the
													// router from the sender
													// (linkstateid)

	public LinkedList<LinkDescription> links = new LinkedList<LinkDescription>();

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
}
