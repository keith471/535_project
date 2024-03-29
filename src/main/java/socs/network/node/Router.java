package socs.network.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import socs.network.exceptions.DuplicateLinkException;
import socs.network.exceptions.NoAvailablePortsException;
import socs.network.exceptions.NoSuchLinkException;
import socs.network.exceptions.SelfLinkException;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.MessageType;
import socs.network.message.SOSPFPacket;
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
		System.out.println(this.lsd.getShortestPath(destinationIP));
	}

	/**
	 * remove the link between this router and the remote router connected at
	 * portNumber. Notice: this command should trigger the synchronization of
	 * database
	 *
	 * @param portNumber
	 *            - the port number which the link attaches at
	 */
	private void processDisconnect(short portNumber) {
		if (this.ports[portNumber] != null) {
			Link l = ports[portNumber];
			// tell the router at port portNumber to remove its link to this router
			this.sendRemoveLink(l, portNumber);
			// upon a successful removal of the remote link, this router will
			// complete the process of removing its own link
		} else {
			System.err.println("ERROR: no link at port " + portNumber);
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
		// updates are sent out? Should be fine I think because the links will
		// have already been established in processAttach, and that's really all
		// we need.

		// update this router's status to two-way
		this.rd.setStatus(RouterStatus.TWO_WAY);

		// send LSAUPDATE out to all neighbors
		// should set sendBack to true
		this.triggerLsaUpdate(true);
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
		if (!this.isStarted()) {
			System.err.println(
					"ERROR: This router is not yet started. You must start this router before using this command.");
			return;
		}

		this.processAttach(processIP, processPort, simulatedIP, weight);

		// set sendBack to false, since we've already been started up
		this.triggerLsaUpdate(true);
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
		ArrayList<ClientThread> threads = new ArrayList<ClientThread>();

		// tell all the routers you're connected with to remove their links to
		// you
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null) {
				ClientThread ct = new ClientThread(this, Protocol.REMOVELINK, this.ports[i].getRouter1(),
						this.ports[i].getRouter2());
				ct.setLinkPort(i);
				ct.setSilentQuit(true);
				threads.add(ct);
			}
		}

		// start all the threads
		for (ClientThread t : threads) {
			t.start();
		}

		// upon successful removal of a link on a remote router, we remove the
		// local link. we wait for all the client threads to terminate before
		// doing so

		for (ClientThread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				System.err.println(
						"WARNING: Main thread interrupted while waiting for client to terminate. May result in errors upon program termination.");
			}
		}

		// all threads have joined; it is safe to exit the program
		System.exit(0);
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
		// add the LinkDescription to the LSA and increment the lsaSeqNumber
		// since we altered the LSA
		lsa.addLink(ld);
		lsa.incrementLsaSeqNumber();
	}

	public void removeLinkDescriptionFromLinkStateDatabase(String remoteIp) {
		LSA lsa = lsd.get_Store().get(rd.getSimulatedIPAddress());
		lsa.removeLink(remoteIp);
		lsa.incrementLsaSeqNumber();
	}
	
	/**
	 * Called by ServerThread in response to receipt of a LSAUPDATE packet
	 * Updates the LinkStateDatabase with any new LSAs in the LSAUPDATE and
	 * propagates the update onto neighbors, if it contains new information
	 * 
	 * @param packet
	 *            - the SOSPFPacket received by the ServerThread
	 */
	public synchronized void performLsaUpdate(SOSPFPacket packet) {
		
		boolean didUpdate = this.lsd.update(packet.getLsaArray());

		if (didUpdate && this.isStarted()) {
			// propagate the LSAUPDATE message to all neighbor routers
			this.propagateLsaUpdate(packet);

			if (packet.getSendBack()) {
				// if the sending router does not have information that this
				// router does, then trigger an LSAUPDATE from this router back
				// to the sending router
				if (this.lsd.containsMore(packet.getLsaArray())) {
					this.triggerTargettedLsaUpdate(packet.getPrecedingNodeIP());
				}
			}
		}
	}

	/**
	 * Called by ServerThread in response to receipt of a LSAUPDATESENDBACK
	 * packet. Updates the LinkStateDatabase with any new LSAs in the LSAUPDATE
	 * 
	 * @param packet
	 *            - the SOSPFPacket received by the ServerThread
	 */
	public synchronized void performLsaUpdateSendBack(SOSPFPacket packet) {
		this.lsd.update(packet.getLsaArray());
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

	public synchronized void removeLinkAtPort(int port) {
		Link l = this.ports[port];

		// remove the link from the ports array
		this.ports[port] = null;

		// remove the link description from the link state database
		this.removeLinkDescriptionFromLinkStateDatabase(l.getRouter2().getSimulatedIPAddress());
	}

	public void reactToRemoveLinkRequest(String sourceIp) {
		// find the port with a link to sourceIp
		int portNumber = this.getPortTo(sourceIp);
		
		// remove the link and notify neighbors with a LSAUPDATE
		this.removeLinkAndUpdateNeighbors(portNumber);
	}

	public void removeLinkAndUpdateNeighbors(int port) {
		// remove the local link
		this.removeLinkAtPort(port);

		// send LSAUPDATE out to all neighbors, since now our LSD has changed
		if (this.isStarted()) {
			// sendBack is false
			this.triggerLsaUpdate(false);
		}
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

	private void triggerLsaUpdate(boolean sendBack) {
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null) {
				this.sendLsaUpdate(ports[i], sendBack);
			}
		}
	}

	private void triggerTargettedLsaUpdate(String target) {
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null) {
				if (this.ports[i].getRouter2().getSimulatedIPAddress().equals(target)) {
					this.sendBackLsaUpdate(ports[i]);
					break;
				}
			}
		}
	}

	/**
	 * Propagate the LSAUPDATE to all neighbors but the neighbor from which we
	 * received the packet
	 * 
	 * @param precedingNodeIP
	 */
	private void propagateLsaUpdate(SOSPFPacket packet) {
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null
					&& !ports[i].getRouter2().getSimulatedIPAddress().equals(packet.getPrecedingNodeIP())) {
				this.forwardLsaUpdate(ports[i], packet);
			}
		}
	}

	private boolean isStarted() {
		return this.rd.getStatus() == RouterStatus.TWO_WAY;
	}

	private int getPortTo(String ip) {
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] != null) {
				if (this.ports[i].getRouter2().getSimulatedIPAddress().equals(ip)) {
					return i;
				}
			}
		}
		// will never reach this
		return -1;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// CLIENT SPAWNERS
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void initiateHandshake(Link l) {
		new ClientThread(this, Protocol.HANDSHAKE, l.getRouter1(), l.getRouter2()).start();
	}

	/**
	 * For sending a new LSAUPDATE (initiated from this router)
	 * 
	 * @param l
	 */
	public void sendLsaUpdate(Link l, boolean sendBack) {
		ClientThread ct = new ClientThread(this, Protocol.LSAUPDATE, l.getRouter1(), l.getRouter2());
		ct.setSendBack(sendBack);
		ct.start();
	}

	/**
	 * For forwarding on an LSAUPDATE that was initiated elsewhere
	 * 
	 * @param packet
	 *            - the packet received by this router
	 */
	public void forwardLsaUpdate(Link l, SOSPFPacket packet) {
		// it does not suffice to just pass on the packet we received, as it
		// could be from a router that was just added to the already-established
		// network. Instead, we need to pass on our entire link state database,
		// which now includes the updates contained in the packet we received.

		// copy our LSD into an array
		Vector<LSA> lsaArray = new Vector<LSA>();
		for (LSA lsa : this.lsd.get_Store().values()) {
			lsaArray.addElement(lsa);
		}
		// create the new packet to forward along
		SOSPFPacket copy = new SOSPFPacket(MessageType.LSAUPDATE, packet.getSrcProcessIP(),
				packet.getSrcProcessPort(), packet.getSrcIP(), lsaArray, false);
		// override preceding node IP with this node's IP
		copy.setPrecedingNodeIP(this.rd.getSimulatedIPAddress());
		// forward it using a ClientThread
		new ClientThread(copy, l.getRouter2()).start();
	}

	public void sendBackLsaUpdate(Link l) {
		new ClientThread(this, Protocol.LSAUPDATESENDBACK, l.getRouter1(), l.getRouter2()).start();
	}

	public void sendAddLink(Link l, int port, int weight) {
		ClientThread ct = new ClientThread(this, Protocol.ADDLINK, l.getRouter1(), l.getRouter2());
		ct.setLinkPort(port);
		ct.setWeight(weight);
		ct.start();
	}

	public void sendRemoveLink(Link l, int port) {
		ClientThread ct = new ClientThread(this, Protocol.REMOVELINK, l.getRouter1(), l.getRouter2());
		ct.setLinkPort(port);
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
					if (!this.isStarted()) {
						String[] cmdLine = command.split(" ");
						processAttach(cmdLine[1], Short.parseShort(cmdLine[2]), cmdLine[3], Short.parseShort(cmdLine[4]));
					}
					else {
						System.err.println("ERROR: You cannot run 'attach' after a router has been started. Please use 'connect' instead.");
					}
				} else if (command.equals("start")) {
					processStart();
				} else if (command.startsWith("connect ")) {
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
