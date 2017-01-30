package socs.network.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import socs.network.exceptions.NoAvailablePortsException;
import socs.network.exceptions.NoSuchLinkException;
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
			// create new Link object
			Link l = new Link(this.rd, rd2);
			// check repeat link
			if (this.isRepeatLink(l)) {
				System.err.println("You've already added an equivalent link");
				return;
			}
			// get a "port" to add a new Link to
			int port = this.getAvailablePort();
			// optimistically add the link to ports (it will be removed if the
			// remote router can't support it)
			this.ports[port] = l;
			// notify the remote router that we'd like to add a link to it
			this.sendAddLink(l, port);
			System.out.println("Successfully added a link to port " + port);
		} catch (NoAvailablePortsException ex) {
			// error, no more ports available
			System.err.println("Error: no more ports available for router " + this.rd.getSimulatedIPAddress());
		}
	}

	/**
	 * Finds an empty port and adds a link to the port
	 * 
	 * @return the number of the port that the link was added to
	 * @throws NoAvailablePortsException
	 *             if this router has no more free ports
	 */
	public synchronized int addLink(Link l) throws NoAvailablePortsException {
		// get an available port
		int port = this.getAvailablePort();
		// add it to ports
		this.ports[port] = l;
		return port;
	}

	public int getAvailablePort() throws NoAvailablePortsException {
		for (int i = 0; i < this.ports.length; i++) {
			if (this.ports[i] == null) {
				return i;
			}
		}
		throw new NoAvailablePortsException();
	}

	public boolean isRepeatLink(Link l) {
		for (Link l2 : this.ports) {
			if (l.equals(l2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Start the router and begin the database synchronization process by
	 * broadcasting HELLO and LSAUPDATE to neighbors
	 */
	private void processStart() {
		// R1 (this router) broadcasts HELLO to all attached routers
		// --> following probably goes elsewhere but is what this triggers <--
		// remote router (R2) receives HELLO: sets the status in its Link's
		// RouterDescription of R1 (??) to INIT, then sends HELLO back to R1
		// R1 receives HELLO from R2 and sets the status in its Link's
		// RouterDescription of R1 (??) as TWO_WAY, and sends HELLO back to R2
		// R2 receives HELLO from R1 and sets the status in its Link's
		// RouterDescription of R1 (??) as TWO_WAY

		// send HELLO
		for (int i = 0; i < this.ports.length; i++) {
			if (!(this.ports[i] == null)) {
				this.initiateHandshake(ports[i]);
			}
		}


		// send LSAUPDATE
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

	/**
	 * A console for the router. General chain of commands: attach -> attach the
	 * new router to other routers start -> starts the router by sending HELLO
	 * messages to all connected routers
	 */
	public void terminal() {
		try {
			InputStreamReader isReader = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isReader);
			System.out.print(">> ");
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
				} else {
					// invalid command
					break;
				}
				System.out.print(">> ");
				command = br.readLine();
			}
			isReader.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initiateHandshake(Link l) {
		new ClientThread(this, Protocol.HANDSHAKE, l.getRouter1(), l.getRouter2()).start();
	}

	public void sendLsaUpdate(Link l) {
		new ClientThread(this, Protocol.LSAUPDATE, l.getRouter1(), l.getRouter2()).start();
	}

	public void sendAddLink(Link l, int port) {
		ClientThread ct = new ClientThread(this, Protocol.ADDLINK, l.getRouter1(), l.getRouter2());
		ct.setLinkPort(port);
		ct.start();
	}

	private Link getLinkFromSourceIp(String sourceIp) {
		for (Link l : this.ports) {
			if (l.getRouter2().getSimulatedIPAddress().equals(sourceIp)) {
				return l;
			}
		}
		// should never happen
		throw new NoSuchLinkException();
	}

	public void updateLinkStatusFromSourceIp(String sourceIp, RouterStatus status) {
		Link l = this.getLinkFromSourceIp(sourceIp);
		l.getRouter2().setStatus(status);
	}

	// /**
	// * Called by ServerThreads of this Router
	// *
	// * @param l
	// * @param port
	// */
	// public void addLink(Link l, int port) {
	// this.ports[port] = l;
	// }

	public void removeLinkAtPort(int port) {
		this.ports[port] = null;
	}

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
