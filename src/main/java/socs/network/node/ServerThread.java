package socs.network.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import socs.network.exceptions.NoAvailablePortsException;
import socs.network.message.SOSPFPacket;

public class ServerThread extends Thread {

	private Router router;
	private Socket socket = null;

	// instantiated with a Socket connection to a client
	public ServerThread(Router router, Socket socket) {
		super("ServerThread");
		this.socket = socket;
		this.router = router;
	}

	@Override
	public void run() {

		try (
				// get the output of the socket to talk to the client
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());) {
			// PrintWriter out = new PrintWriter(socket.getOutputStream(),
			// true);
			// get the input of the socket to listen to the client
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(socket.getInputStream()));) {

			SOSPFPacket inputPacket;
			// in this protocol, the client speaks first, so we just begin
			// listening
			inputPacket = (SOSPFPacket) is.readObject();
			// message received, process it
			switch (inputPacket.getSospfType()) {
			case HELLO:

				break;
			case LSAUPDATE:

				break;
			case ADDLINK:
				handleAddLink(inputPacket, os);
				break;
			case ERROR:

				break;
			default:

			}

			// IMPORTANT: as we did not create the socket connection to the
			// client within a try
			// with resources statement, we are responsible for closing the
			// socket ourselves upon
			// finishing communication
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private void handleAddLink(SOSPFPacket packet, ObjectOutputStream os) throws IOException {

		try {
			// TODO: MIGHT HAVE SERIOUS CONCURRENCY ISSUE HERE
			int port = router.getAvailablePort();
			// If we context switch here, then could have problems
			RouterDescription r2 = new RouterDescription(packet.getSrcProcessIP(), packet.getSrcProcessPort(),
					packet.getSrcIP());
			Link l = new Link(router.getRd(), r2);
			router.addLink(l, port);
			
			// send back success message?
			SOSPFPacket responsePacket = new SOSPFPacket();
			os.writeObject(responsePacket);

		} catch (NoAvailablePortsException ex) {
			// send ERROR response packet
			SOSPFPacket errPacket = new SOSPFPacket("No ports available!!!");
			os.writeObject(errPacket);
		}
	}

	/**
	 * A HELLO has been received. Follow the protocol for dealing with it
	 * 
	 * @throws IOException
	 */
	private void processHello(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {

		// add a Link to R1, retain a reference to this link, and
		// set the status in its Link's RouterDescription of R1 to INIT

		// send HELLO back to R1 and await reply, which should be another HELLO
		SOSPFPacket inputMsg = (SOSPFPacket) in.readObject();

		// ensure HELLO was received, if not then return (ends the
		// communication)
		// if (getMessageType(inputMsg) != MessageType.HELLO) {
		// return;
		// }

		// received HELLO from R1; set the status in R2's Link's
		// RouterDescription of R1 as TWO_WAY

	}

	/**
	 * An LSAUPDATE has been received. Follow the protocol for dealing with it
	 */
	private void processLsaUpdate(ObjectInputStream in, ObjectOutputStream out) {

	}
}
