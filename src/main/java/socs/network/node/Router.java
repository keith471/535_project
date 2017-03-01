package socs.network.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import socs.network.exceptions.DuplicateLinkException;
import socs.network.exceptions.NoAvailablePortsException;
import socs.network.exceptions.NoSuchLinkException;
import socs.network.exceptions.SelfLinkException;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.util.Configuration;

/**
 * This is a "node" in the network. Has packet-forwarding capabilities.
 * 
 * @author kstricks
 *
 */
public class Router {

	// the server the router listens for connection requests with
	private MasterServerThread server;

	// the router's understanding of the network
	// this is what the router runs its shortest path alg over
	private LinkStateDatabase lsd;

	// contains ip address, process port, etc. for this router
	private RouterDescription rd;

	// all routers have 4 ports
	private Link[] ports;

	public Router(Configuration config) {
		this.ports = new Link[4];
		// set this router's simulated IP address using the config file the
		// program has been started with
		this.rd = new RouterDescription(config.getString("socs.network.router.actual_ip"),
				config.getInt("socs.network.router.port"), config.getString("socs.network.router.simulated_ip"));
		// initialize the LSD

		lsd = new LinkStateDatabase(rd);

		// start the server so that it is listening for connection requests
		this.server = new MasterServerThread(this, rd.getProcessPortNumber());
		this.server.start();
	}

	/**
	 * output the shortest path to the given destination ip
	 * <p/>
	 * format: source ip address -> ip address -> ... -> destination ip
	 *
	 * @param destinationIP
	 *            the ip address of the destination simulated router
	 */
	private void processDetect(String destinationIP) {

	}

	/**
	 * remove the link between this router and the remote router connected at
	 * portNumber Notice: this command should trigger the synchronization of
	 * database
	 *
	 * @param portNumber
	 *            the port number which the link attaches at
	 */
	private void processDisconnect(short portNumber) {
		// TODO remove the link between this router and the remote router
		// connected at port number
		// does this require reaching out to the remote router to tell it to
		// also remove its link? Probably

		// send LSAUPDATE out to all neighbors
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null) {
				this.sendLsaUpdate(ports[i]);
			}
		}
	}

	/**
	 * establishes a link to the remote router identified by the given simulated
	 * ip; to establish the connection via socket, you need to identify the
	 * process IP and process Port of the remote router; additionally, weight is
	 * the cost to transmitting data through the link
	 * <p/>
	 * NOTE: this command should NOT trigger link database synchronization
	 * 
	 * @param processIP
	 *            - the real ip address of the remote router
	 * @param processPort
	 *            - the port of the remote process to connect to
	 * @param simulatedIp
	 *            - the simulated ip address of the remote router
	 * @param weight
	 *            - the cost of transmitting through this link
	 */
	private void processAttach(String processIP, short processPort, String simulatedIP, short weight) {

		try {
			// create a router description for the router to connect to
			RouterDescription rd2 = new RouterDescription(processIP, processPort, simulatedIP);
			// create new Link object and add it to ports array
			Link l = new Link(this.rd, rd2, weight);
			int port = this.addLink(l);
			// create new LinkDescription object for this link and add it to the
			// LinkStateDatabase
			this.addLinkDescriptionToLinkStateDatabase(new LinkDescription(rd2.getSimulatedIPAddress(), port, weight));
			// notify the remote router that we'd like to add a link to it
			this.sendAddLink(l, port, weight);
			// notify the remote router
			System.out.println("Successfully added a link to port " + port);
		} catch (NoAvailablePortsException ex) {
			System.err.println("ERROR:\tno more ports available for router " + this.rd.getSimulatedIPAddress());
		} catch (DuplicateLinkException ex) {
			System.err.println("ERROR:\tan equivalent link has already been added (potentially by another router)");
		} catch (SelfLinkException ex) {
			System.err.println("ERROR:\tself-links are not permitted");
		}
	}

	/**
	 * Start the router and begin the database synchronization process by
	 * broadcasting HELLO and LSAUPDATE to neighbors
	 */
	private void processStart() {

		// shake hands with all routers in our ports array
		for (int i = 0; i < this.ports.length; i++) {
			if (!(this.ports[i] == null)) {
				this.initiateHandshake(ports[i]);
			}
		}

		// TODO Could be error here if handshakes are not done before LSA
		// updates are sent out?

		// send LSAUPDATE out to all neighbors
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null) {
				this.sendLsaUpdate(ports[i]);
			}
		}
	}

	/**
	 * add a the link to the remote router, which is identified by the given
	 * simulated ip; to establish the connection via socket, you need to
	 * identify the process IP and process port of the remote router;
	 * additionally, weight is the cost to transmitting data through the link
	 * 
	 * This is essentially just like `attach` except that it also triggers
	 * database synchronization without having to call `start`. Can only be run
	 * after `start` has been run.
	 * 
	 * This command DOES trigger the link database synchronization
	 * 
	 * @param processIP
	 *            - the real ip address of the remote router
	 * @param processPort
	 *            - the port of the remote process to connect to
	 * @param simulatedIp
	 *            - the simulated ip address of the remote router
	 * @param weight
	 *            - the cost of transmitting through this link
	 */
	private void processConnect(String processIP, short processPort, String simulatedIP, short weight) {

	}

	/**
	 * output the simulated ip addresses of the neighbors of the router
	 */
	private void processNeighbors() {
		for (int i = 0; i < this.ports.length; i++) {
			Link l = this.ports[i];
			if (l != null) {
				System.out.println("Port " + i + ": " + l.getRouter2().getSimulatedIPAddress());
			} else {
				System.out.println("Port " + i + ": <empty>");
			}
		}
	}

	/**
	 * disconnect with all neighbors and quit the program
	 * 
	 * NOTE: This DOES trigger synchronization of the link state database
	 */
	private void processQuit() {

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC HELPERS
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Finds an empty port and adds a link to the port
	 * 
	 * @return the number of the port that the link was added to
	 * @throws NoAvailablePortsException
	 *             if this router has no more free ports
	 * @throws DuplicateLinkException
	 *             if the link to be added already has been
	 */
	public synchronized int addLink(Link l)
			throws NoAvailablePortsException, DuplicateLinkException, SelfLinkException {
		// first, ensure that this is not a link back to itself
		if (this.isSelfLink(l)) {
			throw new SelfLinkException();
		}
		// second, ensure that the link is not a duplicate of one we already
		// have
		if (this.isDuplicateLink(l)) {
			throw new DuplicateLinkException();
		}
		// get an available port
		int port = this.getAvailablePort();
		// add it to ports
		this.ports[port] = l;
		return port;
	}
	
	/**
	 * Add the LinkDescription to the LSA of the local router in this router's
	 * LinkStateDatabase
	 * 
	 * @param ld
	 */
	public synchronized void addLinkDescriptionToLinkStateDatabase(LinkDescription ld) {
		// get the LSA for this router
		LSA lsa = lsd.get_Store().get(rd.getSimulatedIPAddress());
		if (lsa != null) {
			// add the LinkDescription to the LSA and increment the lsaSeqNumber since we altered the LSA
			lsa.addLink(ld);
			lsa.incrementLsaSeqNumber();
		} else {
			System.err.println("Could not find matching LSA in Link State Database. This error should not occur");
			System.exit(1);
		}
	}
	
	/**
	 * Update the LinkStateDatabase according to the rules
	 * @param lsaArray
	 * @param sourceIP
	 */
	public synchronized void lsaUpdate(Vector<LSA> lsaArray, String sourceIP) {
		HashMap<String, LSA> lsaMap = lsd.get_Store();
		
		for (LSA lsa : lsaArray) {
			// if the HashMap already contains an LSA with same originating router
			if (lsaMap.containsKey(lsa.getOriginIp())) {
				// check if the lsaSeqNumber of the received LSA is greater, if it is, replace the old LSA
				if (lsaMap.get(lsa.getOriginIp()).getLsaSeqNumber() < lsa.getLsaSeqNumber()) {
					lsaMap.replace(lsa.getOriginIp(), lsa);
				}
			} else { // otherwise add the new LSA
				lsaMap.put(lsa.getOriginIp(), lsa);
			}
		}
		
		// also send LSAUPDATE message to all neighbor routers (except the router that sent the LSAUPDATE)
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null && ports[i].getRouter2().getSimulatedIPAddress() != sourceIP) {
				this.sendLsaUpdate(ports[i]);
			}
		}
		
	}

	/**
	 * Finds an available port by iterating through the ports array
	 * 
	 * @return the index of an available port
	 * @throws NoAvailablePortsException
	 *             if no ports are available
	 */
	public int getAvailablePort() throws NoAvailablePortsException {
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] == null) {
				return i;
			}
		}
		throw new NoAvailablePortsException();
	}

	public void updateLinkStatusFromSourceIp(String sourceIp, RouterStatus status) {
		Link l = this.getLinkFromSourceIp(sourceIp);
		l.getRouter2().setStatus(status);
	}

	public void removeLinkAtPort(int port) {
		this.ports[port] = null;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE HELPERS
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	private boolean isSelfLink(Link l) {
		return l.getRouter2().getProcessIPAddress().equals(this.rd.getProcessIPAddress())
				&& l.getRouter2().getProcessPortNumber() == this.rd.getProcessPortNumber();
	}

	/**
	 * Checks if a link is a duplicate of one already in existence
	 * 
	 * @param l
	 * @return true if an equivalent link exists, false otherwise
	 */
	private boolean isDuplicateLink(Link l) {
		for (Link l2 : this.ports) {
			if (l.equals(l2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Given a remote router's IP address, returns this router's link to the
	 * remote router
	 * 
	 * @param remoteIp
	 * @return
	 */
	private Link getLinkFromSourceIp(String remoteIp) {
		for (Link l : this.ports) {
			if (l.getRouter2().getSimulatedIPAddress().equals(remoteIp)) {
				return l;
			}
		}
		// should never happen
		throw new NoSuchLinkException();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// CLIENT SPAWNERS
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void initiateHandshake(Link l) {
		new ClientThread(this, Protocol.HANDSHAKE, l.getRouter1(), l.getRouter2()).start();
	}

	public void sendLsaUpdate(Link l) {
		new ClientThread(this, Protocol.LSAUPDATE, l.getRouter1(), l.getRouter2()).start();
	}

	public void sendAddLink(Link l, int port, int weight) {
		ClientThread ct = new ClientThread(this, Protocol.ADDLINK, l.getRouter1(), l.getRouter2());
		ct.setLinkPort(port);
		ct.setWeight(weight);
		ct.start();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// TERMINAL
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * A console for the router. General chain of commands: attach -> attach the
	 * new router to other routers start -> starts the router by sending HELLO
	 * messages to all connected routers
	 */
	public void terminal() {
		System.out.println("Terminal for router at " + this.rd.getSimulatedIPAddress());
		try (InputStreamReader isReader = new InputStreamReader(System.in);
				BufferedReader br = new BufferedReader(isReader);) {
			System.out.print("(" + this.rd.getSimulatedIPAddress() + ") >> ");
			// read the first command
			String command = br.readLine();
			// process command and read another (indefinitely)
			while (true) {
				if (command.startsWith("detect ")) {
					String[] cmdLine = command.split(" ");
					processDetect(cmdLine[1]);
				} else if (command.startsWith("disconnect ")) {
					String[] cmdLine = command.split(" ");
					processDisconnect(Short.parseShort(cmdLine[1]));
				} else if (command.startsWith("quit")) {
					processQuit();
				} else if (command.startsWith("attach ")) {
					String[] cmdLine = command.split(" ");
					processAttach(cmdLine[1], Short.parseShort(cmdLine[2]), cmdLine[3], Short.parseShort(cmdLine[4]));
				} else if (command.equals("start")) {
					processStart();
				} else if (command.equals("connect ")) {
					String[] cmdLine = command.split(" ");
					processConnect(cmdLine[1], Short.parseShort(cmdLine[2]), cmdLine[3], Short.parseShort(cmdLine[4]));
				} else if (command.equals("neighbors")) {
					// output neighbors
					processNeighbors();
				} else if (command.equals("exit")) {
					break;
				} else {
					// erroneous command
					System.err.println("ERROR: unrecognized command");
				}
				System.out.print("(" + this.rd.getSimulatedIPAddress() + ") >> ");
				command = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// GETTERS AND SETTERS
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	public MasterServerThread getServer() {
		return server;
	}

	public void setServer(MasterServerThread server) {
		this.server = server;
	}

	public LinkStateDatabase getLsd() {
		return lsd;
	}

	public void setLsd(LinkStateDatabase lsd) {
		this.lsd = lsd;
	}

	public RouterDescription getRd() {
		return rd;
	}

	public void setRd(RouterDescription rd) {
		this.rd = rd;
	}

	public Link[] getPorts() {
		return ports;
	}

	public void setPorts(Link[] ports) {
		this.ports = ports;
	}

}
